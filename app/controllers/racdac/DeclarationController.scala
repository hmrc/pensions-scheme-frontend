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

import connectors._
import controllers.Retrievals
import controllers.actions._
import controllers.racdac.routes.DeclarationController
import identifiers.racdac._
import identifiers.register.SubmissionReferenceNumberId
import models.NormalMode
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Enumerable
import views.html.racdac.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       allowAccess: AllowAccessActionProvider,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: declaration
                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(None) andThen requireData).async {
    implicit request =>
      pensionAdministratorConnector.getPSAName.map { psaName =>
        Ok(
          view(
            psaName = psaName,
            href = DeclarationController.onClickAgree())
        )
      }
  }

  def onClickAgree: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(None) andThen requireData).async {
    implicit request =>
      val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
      val ua = request.userAnswers
        .remove(identifiers.register.DeclarationId).asOpt.getOrElse(request.userAnswers)
        .setOrException(DeclarationId)(true)

      pensionsSchemeConnector.registerScheme(ua, psaId.id).flatMap {
        case Right(submissionResponse) =>
          dataCacheConnector.upsert(
            request.externalId,
            ua.setOrException(SubmissionReferenceNumberId)(submissionResponse).json
          ).map(_ => Redirect(navigator.nextPage(DeclarationId, NormalMode, ua)))
        case Left(_) =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }

  case object MissingPsaId extends Exception("Psa ID missing in request")
}
