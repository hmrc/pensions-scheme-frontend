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
import models.NormalMode
import models.requests.DataRequest
import navigators.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import views.html.racdac.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()( override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       allowAccess: AllowAccessActionProvider,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       emailConnector: EmailConnector,
                                       minimalPsaConnector: MinimalPsaConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: declaration
                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {
  private val logger = Logger(classOf[DeclarationController])
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
      withRACDACName { schemeName =>
        for {
          cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
        } yield Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
      }
  }

  case object MissingPsaId extends Exception("Psa ID missing in request")

  private def sendEmail(psaId: PsaId, schemeName: String)
                       (implicit request: DataRequest[AnyContent]): Future[EmailStatus] = {
    logger.debug("Fetch email from API")

    minimalPsaConnector.getMinimalPsaDetails(psaId.id) flatMap { minimalPsa =>
      emailConnector.sendEmail(
        emailAddress = minimalPsa.email,
        templateName = "pods_racdac_scheme_register",
        params = Map("psaName" -> minimalPsa.name, "schemeName" -> schemeName),
        psaId = psaId
      )
    } recoverWith {
      case _: Throwable => Future.successful(EmailNotSent)
    }
  }

}
