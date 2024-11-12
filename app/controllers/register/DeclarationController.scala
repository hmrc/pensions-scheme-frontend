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

package controllers.register

import audit.{AuditService, TcmpAuditEvent}
import config.FrontendAppConfig
import connectors._
import controllers.Retrievals
import controllers.actions._
import controllers.register.routes.DeclarationController
import identifiers.register._
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyDormantId}
import identifiers.{MoneyPurchaseBenefitsId, SchemeTypeId, TypeOfBenefitsId}
import models.enumerations.SchemeJourneyType
import models.register.DeclarationDormant
import models.register.DeclarationDormant.Yes
import models.register.SchemeType.MasterTrust
import models.requests.DataRequest
import models.{EmptyOptionalSchemeReferenceNumber, NormalMode, PSAMinimalFlags, SchemeReferenceNumber, TypeOfBenefits}
import navigators.Navigator
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Register
import utils.hstasklisthelper.HsTaskListHelperRegistration
import utils.{Enumerable, UserAnswers}
import views.html.register.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpErrorFunctions._

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DeclarationController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       @Register navigator: Navigator,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       emailConnector: EmailConnector,
                                       minimalPsaConnector: MinimalPsaConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                       crypto: ApplicationCrypto,
                                       val view: declaration,
                                       auditService: AuditService
                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with Retrievals
    with I18nSupport
    with Enumerable.Implicits {

  private val logger = Logger(classOf[DeclarationController])

  private def redirects(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Option[Result]] = {
    request.psaId match {
      case None => Future.successful(None)
      case Some(psaId) =>
        minimalPsaConnector.getMinimalFlags(psaId.id).map {
          case PSAMinimalFlags(_, true, false) => Some(Redirect(Call("GET", appConfig.youMustContactHMRCUrl)))
          case PSAMinimalFlags(_, false, true) => Some(Redirect(Call("GET",appConfig.psaUpdateContactDetailsUrl)))
          case _ => None
        }
    }
  }


  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      redirects.flatMap {
        case Some(result) => Future.successful(result)
        case _ =>
        if (hsTaskListHelperRegistration.declarationEnabled(request.userAnswers)) {
          showPage(Ok.apply)
        } else {
          Future.successful(Redirect(controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)))
        }
      }
  }

  private def showPage(status: HtmlFormat.Appendable => Result)
                      (implicit request: DataRequest[AnyContent]): Future[Result] = {

    val isEstCompany = request.userAnswers.hasCompanies(NormalMode)
    val href = DeclarationController.onClickAgree

    val declarationDormantValue = if (isDeclarationDormant) DeclarationDormant.values.head
    else DeclarationDormant.values(1)
    val readyForRender = if (isEstCompany) {
      dataCacheConnector.save(request.externalId, DeclarationDormantId, declarationDormantValue).map(_ => ())
    } else {
      Future.successful(())
    }

    readyForRender.flatMap { _ =>
      request.userAnswers.get(identifiers.DeclarationDutiesId) match {
        case Some(hasWorkingKnowledge) => Future.successful(
          status(
            view(
              isCompany = isEstCompany,
              isDormant = isDeclarationDormant,
              showMasterTrustDeclaration = request.userAnswers.get(SchemeTypeId).contains(MasterTrust),
              hasWorkingKnowledge = hasWorkingKnowledge,
              schemeName = existingSchemeName,
              href = href
            )
          )
        )
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
      }
    }
  }

  private def isDeclarationDormant(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.allEstablishersAfterDelete(
      NormalMode
    ).exists { allEstablishers =>
      allEstablishers.id match {
        case CompanyDetailsId(index) =>
          isDormant(request.userAnswers.get(IsCompanyDormantId(index)))
        case _ =>
          false
      }
    }

  private def isDormant(dormant: Option[DeclarationDormant]): Boolean =
    dormant match {
      case Some(Yes) => true
      case _ => false
    }

  def onClickAgree: Action[AnyContent] = (authenticate() andThen getData() andThen requireData).async {
    implicit request =>
      val psaId: PsaId = request.psaId.getOrElse(throw MissingPsaId)
      val updatedUA = request.userAnswers.remove(identifiers.racdac.DeclarationId).asOpt
        .getOrElse(request.userAnswers)
        .setOrException(DeclarationId)(true)

      (for {
        cacheMap <- dataCacheConnector.upsert(request.externalId, updatedUA.json)
        submissionResponse <- pensionsSchemeConnector.registerScheme(UserAnswers(cacheMap), psaId.id, SchemeJourneyType.NON_RAC_DAC_SCHEME)
        _ <- dataCacheConnector.save(request.externalId, SubmissionReferenceNumberId, submissionResponse)
        _ <- sendEmail(SchemeReferenceNumber(submissionResponse.schemeReferenceNumber), psaId)
        _ <- auditTcmp(psaId.id, request.userAnswers)
      } yield {
        Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
      })recoverWith {
        case ex: UpstreamErrorResponse if is5xx(ex.statusCode) =>
          Future.successful(Redirect(controllers.routes.YourActionWasNotProcessedController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)))
        case _ =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
      }
  }

  private def callbackUrl(psaId: PsaId): String = {
    val encryptedPsa = URLEncoder.encode(crypto.QueryParameterCrypto.encrypt(PlainText(psaId.value)).value, StandardCharsets.UTF_8.toString)
    s"${appConfig.pensionsSchemeUrl}/pensions-scheme/email-response/$encryptedPsa"
  }


  private def sendEmail(srn: SchemeReferenceNumber, psaId: PsaId)
                       (implicit request: DataRequest[AnyContent]): Future[EmailStatus] = {
    logger.debug("Fetch email from API")

    minimalPsaConnector.getMinimalPsaDetails(psaId.id) flatMap { minimalPsa =>
      emailConnector.sendEmail(
        emailAddress = minimalPsa.email,
        templateName = appConfig.emailTemplateId,
        params = Map("srn" -> formatSrnForEmail(srn), "psaName" -> minimalPsa.name),
        psaId = psaId,
        callbackUrl(psaId)
      )
    } recoverWith {
      case _: Throwable => Future.successful(EmailNotSent)
    }
  }

  //scalastyle:off magic.number
  private def formatSrnForEmail(srn: SchemeReferenceNumber): String = {
    //noinspection ScalaStyle
    val (start, end) = srn.id.splitAt(6)
    start + ' ' + end
  }

  case object MissingPsaId extends Exception("Psa ID missing in request")

  private def auditTcmp(psaId: String, ua: UserAnswers)
                       (implicit request: DataRequest[AnyContent]): Future[Unit] =
    Future.successful(
      (
        ua.get(TypeOfBenefitsId),
        ua.get(MoneyPurchaseBenefitsId)
      ) match {
        case (Some(typeOfBenefits), Some(tcmp)) if !typeOfBenefits.equals(TypeOfBenefits.Defined) =>
          auditService.sendExtendedEvent(
            TcmpAuditEvent(
              psaId = psaId,
              tcmp = tcmp.toString,
              payload = ua.json,
              auditType = "TaxationCollectiveMoneyPurchaseSubscriptionAuditEvent"
            )
          )
        case _ => ()
      }
    )

}
