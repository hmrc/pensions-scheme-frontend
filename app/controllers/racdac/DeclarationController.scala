/*
 * Copyright 2024 HM Revenue & Customs
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

import audit.{AuditService, RACDACSubmissionEmailEvent}
import config.FrontendAppConfig
import connectors._
import controllers.Retrievals
import controllers.actions._
import controllers.racdac.routes.DeclarationController
import identifiers.racdac._
import identifiers.register.SubmissionReferenceNumberId
import models.enumerations.SchemeJourneyType
import models.requests.DataRequest
import models.{EmptyOptionalSchemeReferenceNumber, NormalMode, PSAMinimalFlags}
import navigators.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HttpErrorFunctions.is5xx
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Racdac
import utils.{Enumerable, UserAnswers}
import views.html.racdac.declaration

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       @Racdac dataCacheConnector: UserAnswersCacheConnector,
                                       navigator: Navigator,
                                       authenticate: AuthAction,
                                       @Racdac getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       allowAccess: AllowAccessActionProvider,
                                       pensionAdministratorConnector: PensionAdministratorConnector,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       emailConnector: EmailConnector,
                                       minimalPsaConnector: MinimalPsaConnector,
                                       auditService: AuditService,
                                       val controllerComponents: MessagesControllerComponents,
                                       crypto: ApplicationCrypto,
                                       config: FrontendAppConfig,
                                       val view: declaration
                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {

  private val logger = Logger(classOf[DeclarationController])

  private def redirects(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Option[Result]] = {
    request.psaId match {
      case None => Future.successful(None)
      case Some(_) =>
        minimalPsaConnector.getMinimalFlags().map {
          case PSAMinimalFlags(_, true, false) => Some(Redirect(Call("GET", appConfig.youMustContactHMRCUrl)))
          case PSAMinimalFlags(_, false, true) => Some(Redirect(Call("GET", appConfig.psaUpdateContactDetailsUrl)))
          case _ => None
        }
    }
  }

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(EmptyOptionalSchemeReferenceNumber) andThen requireData).async {
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

  def onClickAgree: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(EmptyOptionalSchemeReferenceNumber) andThen requireData).async {
    implicit request =>
      withRACDACName { schemeName =>
        val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
        (for {
          cacheMap <- dataCacheConnector.save(request.externalId, DeclarationId, value = true)
          _ <- register(psaId, schemeName)
        } yield {
          Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
        }) recoverWith {
          case ex: UpstreamErrorResponse if is5xx(ex.statusCode) =>
            Future.successful(Redirect(controllers.racdac.routes.YourActionWasNotProcessedController.onPageLoad()))
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
        }
      }
  }

  private def register(psaId: PsaId, schemeName: String)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val ua = request.userAnswers
      .remove(identifiers.register.DeclarationId).asOpt.getOrElse(request.userAnswers)
      .setOrException(DeclarationId)(true)
    for {
      submissionResponse <- pensionsSchemeConnector.registerScheme(ua, SchemeJourneyType.RAC_DAC_SCHEME)
      _ <- sendEmail(psaId, schemeName)
      _ <- dataCacheConnector.upsert(request.externalId, ua.setOrException(SubmissionReferenceNumberId)(submissionResponse).json)
    } yield {
      Redirect(navigator.nextPage(DeclarationId, NormalMode, ua))
    }
  }

  private def callbackUrl(psaId: PsaId): String = {
    val encryptedPsa = URLEncoder.encode(crypto.QueryParameterCrypto.encrypt(PlainText(psaId.value)).value, StandardCharsets.UTF_8.toString)
    s"${config.pensionsSchemeUrl}/pensions-scheme/email-response-racdac/$encryptedPsa"
  }

  private def sendEmail(psaId: PsaId, schemeName: String)
                       (implicit request: DataRequest[AnyContent]): Future[EmailStatus] = {
    logger.debug("Fetch email from API")
    minimalPsaConnector.getMinimalPsaDetails() flatMap { minimalPsa =>
      emailConnector.sendEmail(
        emailAddress = minimalPsa.email,
        templateName = "pods_racdac_scheme_register",
        params = Map("psaName" -> minimalPsa.name, "schemeName" -> schemeName),
        psaId = psaId,
        callbackUrl(psaId)
      ).map { status =>
        auditService.sendEvent(RACDACSubmissionEmailEvent(psaId, minimalPsa.email))
        status
      }
    } recoverWith {
      case _: Throwable => Future.successful(EmailNotSent)
    }
  }

  case object MissingPsaId extends Exception("Psa ID missing in request")
}
