/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.{FakeUserAnswersCacheConnector, _}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationFormProvider
import helpers.DataCompletionHelper
import identifiers._
import identifiers.register.{DeclarationDormantId, DeclarationId}
import models._
import models.register.{DeclarationDormant, SchemeSubmissionResponse, SchemeType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HttpReads.upstreamResponseMessage
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.hstasklisthelper.HsTaskListHelperRegistration
import utils.{FakeNavigator, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  import DeclarationControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(mockHsTaskListHelperRegistration)
    when(mockHsTaskListHelperRegistration.declarationEnabled(any())).thenReturn(true)
  }

  "Declaration Controller" must {

    "redirect to task list page when user answers are not complete" in {
      when(mockHsTaskListHelperRegistration.declarationEnabled(any())).thenReturn(false)
      val result = controller(UserAnswers().schemeName("Test Scheme").dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None).url
    }

    "redirect to you must contact HMRC page when deceased flag is true" in {
      val result = controller(dataRetrievalAction = individualEst, isDeceased = true).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe frontendAppConfig.youMustContactHMRCUrl
    }

    "redirect to you must update your address page when rls flag is true" in {
      val result = controller(dataRetrievalAction = individualEst, rlsFlag = true).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe frontendAppConfig.psaUpdateContactDetailsUrl
    }

    "return OK and don't save the DeclarationDormant " when {

      "the establisher is an individual" in {
        val result = controller(individualEst).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false)
        FakeUserAnswersCacheConnector.verifyNot(DeclarationDormantId)
      }
    }

    "return OK, the correct view and save the DeclarationDormant" when {

      "the establisher is a dormant company" in {
        val result = controller(dormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values.head)
      }

      "the establisher is non dormant company" in {
        val result = controller(nonDormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = false)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values(1))
      }
    }

    "return OK and the correct view " when {
      "master trust and all the answers is complete" in {

        val result = controller(dataWithMasterTrust).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false, showMasterTrustDeclaration = true)
      }
    }

    "redirect to the next page on clicking agree and continue and ensure racdac declaration ID removed and register declaration ID present" in {
      when(mockPensionSchemeConnector.registerScheme(any(), any(), any())(any(), any())).thenReturn(Future.successful(validSchemeSubmissionResponse))
      val result = controller(nonDormantCompany).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      val upsertedUA = UserAnswers(FakeUserAnswersCacheConnector.getUpsertedData.get)
      upsertedUA.get(racdac.DeclarationId) mustBe None
      upsertedUA.get(DeclarationId) mustBe Some(true)
    }

    "redirect to your action was not processed page when backend returns 5XX" in {
      reset(mockPensionSchemeConnector)
      when(mockPensionSchemeConnector.registerScheme(any(), any(), any())(any(), any())).thenReturn(Future.failed(
        UpstreamErrorResponse(upstreamResponseMessage("POST", "url",
          Status.INTERNAL_SERVER_ERROR, "response.body"), Status.INTERNAL_SERVER_ERROR, Status.INTERNAL_SERVER_ERROR)))
      val result = controller(nonDormantCompany).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.YourActionWasNotProcessedController.onPageLoad(NormalMode, None).url)
    }

    "redirect to session timeout page when backend returns any other error than 5XX" in {
      reset(mockPensionSchemeConnector)
      when(mockPensionSchemeConnector.registerScheme(any(), any(), any())(any(), any())).thenReturn(Future.failed(
        UpstreamErrorResponse(upstreamResponseMessage("POST", "url",
          Status.BAD_REQUEST, "response.body"), Status.BAD_REQUEST, Status.BAD_REQUEST)))
      val result = controller(nonDormantCompany).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page on clicking agree and continue and audit TCMP" in {
      reset(mockAuditService, mockPensionSchemeConnector)
      when(mockPensionSchemeConnector.registerScheme(any(), any(), any())(any(), any())).thenReturn(Future.successful(validSchemeSubmissionResponse))
      val result = controller(tcmpAuditDataUa(TypeOfBenefits.MoneyPurchase).dataRetrievalAction).onClickAgree()(fakeRequest)

      val argCaptor = ArgumentCaptor.forClass(classOf[TcmpAuditEvent])

      val auditEvent = TcmpAuditEvent(
        psaId = "A0000000",
        tcmp = "01",
        payload = Json.obj(
          "moneyPurchaseBenefits" -> "01",
          "benefits" -> "opt1",
          SchemeNameId.toString -> "schemeName",
          "declaration" -> true
        ) ++ tcmpAuditDataUa(TypeOfBenefits.MoneyPurchase).json.as[JsObject],
        auditType = "TaxationCollectiveMoneyPurchaseSubscriptionAuditEvent"
      )

      whenReady(result) {
        response =>
          response.header.status mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockAuditService, times(1)).sendExtendedEvent(argCaptor.capture())(any(), any())
          argCaptor.getValue mustBe auditEvent
      }
    }

    "redirect to the next page on clicking agree and continue and not audit TCMP when TypeOfBenefit is Defined" in {
      reset(mockAuditService)

      val result = controller(tcmpAuditDataUa(TypeOfBenefits.Defined).dataRetrievalAction).onClickAgree()(fakeRequest)

      whenReady(result) {
        response =>
          response.header.status mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(mockAuditService, times(0)).sendExtendedEvent(any())(any(), any())
      }
    }

    "send an email on clicking agree and continue" which {
      "fetches from Get PSA Minimal Details" in {

        reset(mockEmailConnector)

        when(mockEmailConnector.sendEmail(eqTo("test@test.com"), eqTo("pods_scheme_register"), any(), any(),any())(any(), any()))
          .thenReturn(Future.successful(EmailSent))

        whenReady(controller(nonDormantCompany, fakeEmailConnector = mockEmailConnector).onClickAgree()(fakeRequest)) { _ =>

          verify(mockEmailConnector, times(1)).sendEmail(
            eqTo("test@test.com"),
            eqTo("pods_scheme_register"),
            eqTo(Map("srn" -> "S12345 67890", "psaName" -> "psa name")),
            eqTo(psaId),any()
          )(any(), any())

        }
      }
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "POST" in {
          val result = controller(dontGetAnyData).onClickAgree()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }

  }

}

object DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar with DataCompletionHelper {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new DeclarationFormProvider()
  private val form = formProvider()
  private val href = controllers.register.routes.DeclarationController.onClickAgree
  val psaId = PsaId("A0000000")

  private val mockHsTaskListHelperRegistration = mock[HsTaskListHelperRegistration]
  private val mockAuditService = mock[AuditService]
  private val mockPensionSchemeConnector = mock[PensionsSchemeConnector]

  private val view = injector.instanceOf[declaration]

  private def uaWithBasicData: UserAnswers =
    setCompleteBeforeYouStart(
      isComplete = true,
      setCompleteMembers(
        isComplete = true,
        setCompleteBank(
          isComplete = true,
          setCompleteBenefits(
            isComplete = true,
            setCompleteEstIndividual(0, UserAnswers())
          )
        )
      )
    )
      .set(HaveAnyTrusteesId)(false).asOpt.value

  private def controller(dataRetrievalAction: DataRetrievalAction,
                         fakeEmailConnector: EmailConnector = fakeEmailConnector,
                         isSuspended:Boolean = true, isDeceased:Boolean = false, rlsFlag:Boolean = false
                        ): DeclarationController =
    new DeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockPensionSchemeConnector,
      fakeEmailConnector,
      fakeMinimalPsaConnector(isSuspended, isDeceased, rlsFlag),
      controllerComponents,
      mockHsTaskListHelperRegistration,
      crypto,
      view,
      mockAuditService
    )

  private def viewAsString(form: Form[_] = form, isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false): String =
    view(
      isCompany,
      isDormant,
      showMasterTrustDeclaration,
      hasWorkingKnowledge,
      Some("Test Scheme"),
      href
    )(fakeRequest, messages).toString

  private def individualEst: DataRetrievalAction = {
    setCompleteWorkingKnowledge(isComplete = true, uaWithBasicData)
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value
      .dataRetrievalAction
  }

  private def dataWithMasterTrust: DataRetrievalAction = {
    setCompleteWorkingKnowledge(isComplete = true, uaWithBasicData)
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value.schemeType(SchemeType.MasterTrust).set(HaveAnyTrusteesId)(false).asOpt.value
      .dataRetrievalAction
  }

  private def tcmpAuditDataUa(typeOfBenefit: TypeOfBenefits): UserAnswers =
    setCompleteWorkingKnowledge(
      isComplete = true,
      ua = setCompleteEstCompany(1, uaWithBasicData)
    )
      .set(identifiers.DeclarationDutiesId)(false)
      .asOpt
      .value
      .establisherCompanyDormant(1, DeclarationDormant.No)
      .set(MoneyPurchaseBenefitsId)(MoneyPurchaseBenefits.Collective)
      .asOpt
      .value
      .set(TypeOfBenefitsId)(typeOfBenefit)
      .asOpt
      .value
      .set(DeclarationId)(true)
      .asOpt
      .value

  private val nonDormantCompany: DataRetrievalAction =
    setCompleteWorkingKnowledge(
      isComplete = true,
      ua = setCompleteEstCompany(1, uaWithBasicData)
    )
      .set(identifiers.DeclarationDutiesId)(false)
      .asOpt
      .value
      .setOrException(racdac.DeclarationId)(true)
      .establisherCompanyDormant(1, DeclarationDormant.No)
      .dataRetrievalAction

  private val dormantCompany: DataRetrievalAction = {
    setCompleteWorkingKnowledge(
      isComplete = true, setCompleteEstCompany(1, uaWithBasicData))
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value.establisherCompanyDormant(1, DeclarationDormant.Yes).dataRetrievalAction
  }

  private val mockEmailConnector = mock[EmailConnector]

  private val validSchemeSubmissionResponse = SchemeSubmissionResponse("S1234567890")

  private val fakeEmailConnector = new EmailConnector {
    override def sendEmail
    (emailAddress: String, templateName: String, params: Map[String, String] = Map.empty, psaId: PsaId,callbackUrl: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmailStatus] = {
      Future.successful(EmailSent)
    }
  }

  private def fakeMinimalPsaConnector(isSuspended: Boolean, isDeceased:Boolean, rlsFlag:Boolean) = new MinimalPsaConnector {
    override def getMinimalFlags(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PSAMinimalFlags] =
      Future.successful(PSAMinimalFlags(isSuspended = isSuspended, isDeceased = isDeceased, rlsFlag = rlsFlag))

    override def getMinimalPsaDetails(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MinimalPSA] =
      Future.successful(MinimalPSA("test@test.com", isPsaSuspended = isSuspended, Some("psa name"), None))

    override def getPsaNameFromPsaID(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] =
      Future.successful(Some("psa name"))
  }
}
