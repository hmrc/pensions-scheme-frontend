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
import connectors.{PensionSchemeVarianceLockConnector, PensionsSchemeConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.{PstrId, SchemeNameId, VariationDeclarationId}
import javax.inject.Inject
import models.UpdateMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.variationDeclaration

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       @Register navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: DeclarationFormProvider,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       lockConnector: PensionSchemeVarianceLockConnector,
                                       updateSchemeCacheConnector: UpdateSchemeCacheConnector
       )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()
  val postCall = routes.VariationDeclarationController.onSubmit _

  def onPageLoad(srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(UpdateMode, srn) andThen requireData).async {
    implicit request =>
      Future.successful(Ok(variationDeclaration(appConfig, form, request.userAnswers.get(SchemeNameId), postCall(srn), srn)))
  }

  def onSubmit(srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(UpdateMode, srn) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(variationDeclaration(appConfig, formWithErrors, request.userAnswers.get(SchemeNameId), postCall(srn), srn))),
        _ => {
          srn.flatMap { srnId =>
            request.userAnswers.get(PstrId).map {
            pstr =>
              for {
                _ <- pensionsSchemeConnector.updateSchemeDetails(request.psaId.id, pstr, request.userAnswers)
                _ <- updateSchemeCacheConnector.removeAll(srnId)
                _ <- lockConnector.releaseLock(request.psaId.id, srnId)
              } yield {
                Redirect(navigator.nextPage(VariationDeclarationId, UpdateMode, UserAnswers()))
              }
            }
          }.getOrElse(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad)))

        }
      )
  }


}