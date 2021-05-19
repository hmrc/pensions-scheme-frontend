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

import config.FrontendAppConfig
import connectors._
import controllers.Retrievals
import controllers.actions._
import controllers.racdac.routes.DeclarationController
import identifiers.racdac._
import identifiers.register.SubmissionReferenceNumberId
import models.NormalMode
import models.requests.DataRequest
import navigators.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Enumerable, UserAnswers}
import views.html.racdac.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       allowAccess: AllowAccessActionProvider,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
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

  private def redirects(implicit request: DataRequest[AnyContent], hc: HeaderCarrier):Future[Option[Result]] = {
    request.psaId match {
      case None => Future.successful(None)
      case Some(psaId) =>
        minimalPsaConnector.getMinimalFlags(psaId.id).map { mf =>
          if (mf.isDeceased) {
            Some(Redirect(appConfig.youMustContactHMRCUrl))
          } else {
            None
          }
        }
    }
  }

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(None) andThen requireData).async {
    implicit request =>
      redirects.flatMap {
        case Some(result) => Future.successful(result)
        case _ =>
          pensionAdministratorConnector.getPSAName.map { psaName =>
            Ok(
              view(
                psaName = psaName,
                href = DeclarationController.onClickAgree())
            )
          }
      }
  }

  def onClickAgree: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(None) andThen requireData).async {
    implicit request =>
      withRACDACName { schemeName =>
        val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
        for {
          cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
          _ <- register(psaId, schemeName)
        } yield {
          Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
        }
      }
  }

  private def register(psaId: PsaId, schemeName: String)(implicit request: DataRequest[AnyContent]):Future[Result] = {
    val ua = request.userAnswers
      .remove(identifiers.register.DeclarationId).asOpt.getOrElse(request.userAnswers)
      .setOrException(DeclarationId)(true)
    pensionsSchemeConnector.registerScheme(ua, psaId.id).flatMap {
      case Right(submissionResponse) =>
        sendEmail(psaId, schemeName).flatMap { _ =>
          dataCacheConnector.upsert(
            request.externalId,
            ua.setOrException(SubmissionReferenceNumberId)(submissionResponse).json
          ).map(_ => Redirect(navigator.nextPage(DeclarationId, NormalMode, ua)))
        }
      case Left(_) =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }

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

  case object MissingPsaId extends Exception("Psa ID missing in request")
}
