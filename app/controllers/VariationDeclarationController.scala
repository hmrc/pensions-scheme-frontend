/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, PensionsSchemeConnector, SchemeDetailsReadOnlyCacheConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.{PstrId, SchemeNameId, VariationDeclarationId}
import javax.inject.Inject
import models.UpdateMode
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, UserAnswers}
import views.html.variationDeclaration

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationController @Inject()(
                                                appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                @Register navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                formProvider: DeclarationFormProvider,
                                                pensionsSchemeConnector: PensionsSchemeConnector,
                                                lockConnector: PensionSchemeVarianceLockConnector,
                                                updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                viewConnector: SchemeDetailsReadOnlyCacheConnector
                                              )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()
  val postCall = routes.VariationDeclarationController.onSubmit _

  def onPageLoad(srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(UpdateMode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      srn.fold(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))) { actualSrn =>
        updateSchemeCacheConnector.fetch(actualSrn).map {
          case Some(_) => Ok(variationDeclaration(appConfig, form, request.userAnswers.get(SchemeNameId), postCall(srn), srn))
          case _ => Redirect(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn))
        }
      }
  }

  def onSubmit(srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(UpdateMode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(variationDeclaration(appConfig, formWithErrors, request.userAnswers.get(SchemeNameId), postCall(srn), srn))),
        value => {
          srn.flatMap { srnId =>
            request.userAnswers.get(PstrId).map {
              pstr =>
                val ua = request.userAnswers.set(VariationDeclarationId)(value).asOpt.getOrElse(request.userAnswers)
                for {
                  _ <- pensionsSchemeConnector.updateSchemeDetails(request.psaId.id, pstr, ua)
                  _ <- updateSchemeCacheConnector.removeAll(srnId)
                  _ <- viewConnector.removeAll(request.externalId)
                  _ <- lockConnector.releaseLock(request.psaId.id, srnId)
                } yield {
                  Redirect(navigator.nextPage(VariationDeclarationId, UpdateMode, UserAnswers(), srn))
                }
            }
          }.getOrElse(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad)))

        }
      )
  }


}