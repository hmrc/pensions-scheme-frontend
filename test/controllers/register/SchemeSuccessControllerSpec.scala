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

package controllers.register

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.racdac.RACDACNameId
import identifiers.register.SubmissionReferenceNumberId
import models.register.SchemeSubmissionResponse
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.mvc.Results._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import views.html.register.schemeSuccess

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SchemeSuccessControllerSpec extends ControllerSpecBase with MockitoSugar {

  override lazy val app: Application = new GuiceApplicationBuilder().configure(
    "features.useManagePensionsFrontend" -> true
  ).build()

  private lazy val onwardRoute = frontendAppConfig.managePensionsSchemeOverviewUrl

  private val submissionReferenceNumber = "XX123456789132"

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val fakePensionAdminstratorConnector = new PensionAdministratorConnector {
    override def getPSAEmail(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("email@test.com")

    override def getPSAName(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("PSA Name")
  }

  private val schemeDataForNormalScheme: JsObject =
    UserAnswers()
      .set(SubmissionReferenceNumberId)(SchemeSubmissionResponse(submissionReferenceNumber)).asOpt.get
      .set(RACDACNameId)("").asOpt.get.json.as[JsObject]

  private val view = injector.instanceOf[schemeSuccess]

  private def controller(dataRetrievalAction: DataRetrievalAction =
                         new FakeDataRetrievalAction(Some(schemeDataForNormalScheme))): SchemeSuccessController =
    new SchemeSuccessController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakePensionAdminstratorConnector,
      controllerComponents,
      view
    )

  def viewAsString(): String =
    view(
      LocalDate.now(),
      submissionReferenceNumber,
      showMasterTrustContent = false,
      "email@test.com"
    )(fakeRequest, messages).toString

  appRunning()

  "SchemeSuccess Controller" must {

    "return OK and the correct view for a GET" in {
      when(mockUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))

      val result = controller().onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(mockUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page for a POST and verify that the mongo db has values removed" in {
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      val expectedJsValue = UserAnswers()
        .set(SubmissionReferenceNumberId)(SchemeSubmissionResponse(submissionReferenceNumber)).asOpt.get.json

      verify(mockUserAnswersCacheConnector, times(1)).upsert(any(), Matchers.eq(expectedJsValue))(any(), any())

    }
  }

}
