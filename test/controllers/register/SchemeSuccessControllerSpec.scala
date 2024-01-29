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

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.SchemeNameId
import identifiers.racdac.{ContractOrPolicyNumberId, RACDACNameId}
import identifiers.register.SubmissionReferenceNumberId
import models.register.SchemeSubmissionResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsNull, JsValue}
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

  val submissionReferenceNumber = "XX123456789132"

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val fakePensionAdminstratorConnector = new PensionAdministratorConnector {
    override def getPSAEmail(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("email@test.com")

    override def getPSAName(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("PSA Name")
  }

  private val nonRACDACSchemeName = "scheme"
  private val racDACSchemeName = "racdac scheme"
  private val racDACContractNo = "121212"

  private def schemeDataForNormalScheme(
                                       racDACSchemeName: Option[String] = None,
                                       racDACContract: Option[String] = None
                                       ):UserAnswers = {
    val ua = UserAnswers()
      .set(SchemeNameId)(nonRACDACSchemeName).asOpt.get
      .set(SubmissionReferenceNumberId)(SchemeSubmissionResponse(submissionReferenceNumber)).asOpt.get

    val uaUpdatedWithName = racDACSchemeName match {
      case Some(sn) => ua.set(RACDACNameId)(sn).asOpt.get
      case None => ua
    }

    val uaFinalUpdated = racDACContract match {
      case Some(cn) => uaUpdatedWithName.set(ContractOrPolicyNumberId)(cn).asOpt.get
      case None => uaUpdatedWithName
    }

    uaFinalUpdated
  }

  private val view = injector.instanceOf[schemeSuccess]

  private def controller(data:Option[UserAnswers]): SchemeSuccessController = {
    val dataRetrievalAction: DataRetrievalAction =
      new FakeDataRetrievalAction(data.map(_.json))
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
  }

  def viewAsString(): String =
    view(
      LocalDate.now(),
      submissionReferenceNumber,
      showMasterTrustContent = false,
      "email@test.com"
    )(fakeRequest, messages).toString

  appRunning()

  "SchemeSuccess Controller" must {

    "return OK and the correct view for a GET when there are no RAC/DAC answers" in {
      reset(mockUserAnswersCacheConnector)
      when(mockUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      when(mockUserAnswersCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(JsNull))


      val result = controller(Some(schemeDataForNormalScheme())).onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(mockUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsValue])

      verify(mockUserAnswersCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
      val actualUserAnswers = UserAnswers(jsonCaptor.getValue)
      actualUserAnswers.get(RACDACNameId).isDefined mustBe false
      actualUserAnswers.get(ContractOrPolicyNumberId).isDefined mustBe false
      actualUserAnswers.get(SchemeNameId).isDefined mustBe false
    }

  "return OK and the correct view for a GET when RACDACNameId exists only" in {
      reset(mockUserAnswersCacheConnector)
      when(mockUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      when(mockUserAnswersCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(JsNull))

      val result = controller(Some(schemeDataForNormalScheme(racDACSchemeName = Some(racDACSchemeName)))).onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(mockUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsValue])

      verify(mockUserAnswersCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
      val actualUserAnswers = UserAnswers(jsonCaptor.getValue)
      actualUserAnswers.get(RACDACNameId).isDefined mustBe true
      actualUserAnswers.get(ContractOrPolicyNumberId).isDefined mustBe false
      actualUserAnswers.get(SchemeNameId).isDefined mustBe false
    }

  "return OK and the correct view for a GET when both RAC/DAC answers exist" in {
    reset(mockUserAnswersCacheConnector)
      when(mockUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      when(mockUserAnswersCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(JsNull))


    val result = controller(Some(
        schemeDataForNormalScheme(
          racDACSchemeName = Some(racDACSchemeName),
          racDACContract = Some(racDACContractNo)
        )
    )).onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(mockUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsValue])

      verify(mockUserAnswersCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
      val actualUserAnswers = UserAnswers(jsonCaptor.getValue)
      actualUserAnswers.get(RACDACNameId).isDefined mustBe true
      actualUserAnswers.get(ContractOrPolicyNumberId).isDefined mustBe true
      actualUserAnswers.get(SchemeNameId).isDefined mustBe false
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(None).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page for a POST" in {
      val result = controller(None).onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

    }
  }

}
