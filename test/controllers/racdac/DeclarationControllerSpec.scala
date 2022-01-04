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

package controllers.racdac

import audit.{AuditService, RACDACSubmissionEmailEvent}
import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, _}
import controllers.ControllerSpecBase
import controllers.actions._
import helpers.DataCompletionHelper
import identifiers.racdac.{DeclarationId, RACDACNameId}
import identifiers.register.SubmissionReferenceNumberId
import models.register.SchemeSubmissionResponse
import models.{MinimalPSA, PSAMinimalFlags}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, ArgumentMatchers, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HttpReads.upstreamResponseMessage
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.{FakeNavigator, UserAnswers}
import views.html.racdac.declaration

import scala.concurrent.Future

class DeclarationControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  import DeclarationControllerSpec._

  override protected def beforeEach(): Unit = {
    when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
  }

  "onPageLoad" must {
    "return OK and the correct view " in {
      when(mockMinimalPsaConnector.getMinimalFlags(any())(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = false, rlsFlag = false)))
        val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
    }

    "redirect to you must contact HMRC page when deceased flag is true" in {
      when(mockMinimalPsaConnector.getMinimalFlags(any())(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = true, rlsFlag = false)))
      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe frontendAppConfig.youMustContactHMRCUrl
    }
    "redirect to you must update your address page when rls flag is true" in {
      when(mockMinimalPsaConnector.getMinimalFlags(any())(any(), any()))
        .thenReturn(Future.successful(PSAMinimalFlags(isSuspended = false, isDeceased = false, rlsFlag = true)))
      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe frontendAppConfig.psaUpdateContactDetailsUrl
    }
  }

  "onClickAgree" must {
    "redirect to the next page on clicking agree and continue" in {
      val uaCaptorForRegisterScheme = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockPensionsSchemeConnector.registerScheme(uaCaptorForRegisterScheme.capture(), any(), any())(any(), any()))
        .thenReturn(Future.successful(schemeSubmissionResponse))
      when(mockEmailConnector.sendEmail(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(EmailSent))
      when(mockMinimalPsaConnector.getMinimalPsaDetails(any())(any(), any())).thenReturn(Future.successful(minimalPsa))
      doNothing.when(mockAuditService).sendEvent(any())(any(), any())

      val result = controller(dataRetrievalAction).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(mockPensionsSchemeConnector, times(1)).registerScheme(any(), any(), any())(any(), any())
      uaCaptorForRegisterScheme.getValue.get(DeclarationId) mustBe Some(true)
      FakeUserAnswersCacheConnector.verifyUpsert(DeclarationId, true)
      FakeUserAnswersCacheConnector.verifyUpsert(SubmissionReferenceNumberId, schemeSubmissionResponse)
      verify(mockEmailConnector, times(1))
        .sendEmail(ArgumentMatchers.eq(minimalPsa.email), ArgumentMatchers.eq("pods_racdac_scheme_register"),
          ArgumentMatchers.eq(emailParams), any(), any())(any(), any())
      val expectedAuditEvent = RACDACSubmissionEmailEvent(psaId,minimalPsa.email )
      verify(mockAuditService,times(1)).sendEvent(ArgumentMatchers.eq(expectedAuditEvent))(any(),any())

    }

    "redirect to your action was not processed page when backend returns 5XX" in {
      reset(mockPensionsSchemeConnector)
      when(mockPensionsSchemeConnector.registerScheme(any(), any(), any())(any(), any())).thenReturn(Future.failed(
        UpstreamErrorResponse(upstreamResponseMessage("POST", "url",
          Status.INTERNAL_SERVER_ERROR, "response.body"), Status.INTERNAL_SERVER_ERROR, Status.INTERNAL_SERVER_ERROR)))

      val result = controller(dataRetrievalAction).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.racdac.routes.YourActionWasNotProcessedController.onPageLoad().url)
    }

    "redirect to session timeout page when backend returns any other error than 5XX" in {
      reset(mockPensionsSchemeConnector)
      when(mockPensionsSchemeConnector.registerScheme(any(), any(), any())(any(), any())).thenReturn(Future.failed(
        UpstreamErrorResponse(upstreamResponseMessage("POST", "url",
          Status.BAD_REQUEST, "response.body"), Status.BAD_REQUEST, Status.BAD_REQUEST)))
      val result = controller(dataRetrievalAction).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}

object DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar with DataCompletionHelper {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val schemeName = "scheme"
  private val minimalPsa = MinimalPSA(email = "a@a.c", isPsaSuspended = false, organisationName = Some("org"), individualDetails = None)
  private val emailParams = Map("psaName" -> minimalPsa.name, "schemeName" -> schemeName)
  private val href = controllers.racdac.routes.DeclarationController.onClickAgree()
  private val mockPensionAdministratorConnector = mock[PensionAdministratorConnector]
  private val mockEmailConnector = mock[EmailConnector]
  private val mockAuditService = mock[AuditService]
  private val mockMinimalPsaConnector = mock[MinimalPsaConnector]
  private val mockPensionsSchemeConnector = mock[PensionsSchemeConnector]
  private val psaName = "A PSA"
  private val psaId = PsaId("A0000000")
  private val view = injector.instanceOf[declaration]
  private val mockAppConfig = mock[FrontendAppConfig]

  private val schemeSubmissionResponse = SchemeSubmissionResponse(schemeReferenceNumber = "srn")

  private def controller(dataRetrievalAction: DataRetrievalAction): DeclarationController =
    new DeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeAllowAccessProvider(),
      mockPensionAdministratorConnector,
      mockPensionsSchemeConnector,
      mockEmailConnector,
      mockMinimalPsaConnector,
      mockAuditService,
      controllerComponents,
      crypto,
      mockAppConfig,
      view
    )

  private def viewAsString(): String =
    view(
      psaName,
      href
    )(fakeRequest, messages).toString

  private def dataRetrievalAction: DataRetrievalAction = {
    UserAnswers()
      .set(RACDACNameId)(schemeName).asOpt.get
      .dataRetrievalAction
  }
}
