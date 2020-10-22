/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.routes.VariationDeclarationController
import identifiers.{PstrId, SchemeNameId, VariationDeclarationId}
import javax.inject.Inject
import models.UpdateMode
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Register
import utils.{Enumerable, UserAnswers}
import views.html.variationDeclaration

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                @Register navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                pensionsSchemeConnector: PensionsSchemeConnector,
                                                lockConnector: PensionSchemeVarianceLockConnector,
                                                updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: variationDeclaration
                                              )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController
  with Retrievals with I18nSupport with Enumerable.Implicits {

  def onPageLoad(srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(UpdateMode, srn) andThen
    allowAccess(srn) andThen requireData).async {
    implicit request =>
      val href = VariationDeclarationController.onClickAgree(srn)
      srn.fold(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))) { actualSrn =>
        updateSchemeCacheConnector.fetch(actualSrn).map {
          case Some(_) => Ok(view(request.userAnswers.get(SchemeNameId), srn, href))
          case _ => Redirect(controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn))
        }
      }
  }

  def onClickAgree(srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(UpdateMode, srn) andThen
    requireData).async {
    implicit request =>
      val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
      srn.flatMap { srnId =>
        request.userAnswers.get(PstrId).map {
          pstr =>
            val ua = request.userAnswers.set(VariationDeclarationId)(value = true).asOpt.getOrElse(request.userAnswers)
            for {
              _ <- pensionsSchemeConnector.updateSchemeDetails(psaId.id, pstr, ua).map(_.status == OK)
              _ <- updateSchemeCacheConnector.removeAll(srnId)
              _ <- viewConnector.removeAll(request.externalId)
              _ <- lockConnector.releaseLock(psaId.id, srnId)
            } yield {
              Redirect(navigator.nextPage(VariationDeclarationId, UpdateMode, UserAnswers(), srn))
            }
        }
      }.getOrElse(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))

  }

  case object MissingPsaId extends Exception("Psa ID missing in request")
}
