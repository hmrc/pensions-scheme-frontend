/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.racdac

import audit.AuditService
import config.FrontendAppConfig
import connectors._
import controllers.Retrievals
import controllers.actions._
import controllers.register.routes.DeclarationController
import identifiers.register._
import models.NormalMode
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Register
import utils.{Enumerable, UserAnswers}
import views.html.racdac.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       @Register navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: declaration
                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      pensionAdministratorConnector.getPSAName.map { psaName =>
        Ok(
          view(
            psaName = psaName,
            href = DeclarationController.onClickAgree())
        )
      }
  }

  def onClickAgree: Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
      (for {
        cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
        eitherSubmissionResponse <- pensionsSchemeConnector.registerScheme(UserAnswers(cacheMap), psaId.id)
      } yield eitherSubmissionResponse).flatMap {
        case Right(submissionResponse) =>
          for {
            cacheMap <- dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse)
          } yield Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
        case Left(_) =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }
  case object MissingPsaId extends Exception("Psa ID missing in request")


}
