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

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.SchemeNameId
import identifiers.racdac.{ContractOrPolicyNumberId, DeclarationId, RACDACNameId}
import org.mockito.ArgumentMatchers._
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsNull, JsObject, JsValue}
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.racdac.schemeSuccess

import scala.concurrent.Future

class SchemeSuccessControllerSpec extends ControllerSpecBase with MockitoSugar {

  override lazy val app: Application = new GuiceApplicationBuilder().configure(
    "features.useManagePensionsFrontend" -> true
  ).build()

  private lazy val onwardRoute = frontendAppConfig.managePensionsSchemeOverviewUrl
  private val email = "email@a.com"
  private val nonRacDACSchemeName = "schemeName 2"
  private val racDACSchemeName = "schemeName"

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val mockPensionAdminstratorConnector = mock[PensionAdministratorConnector]

  private val schemeDataForNormalScheme: JsObject =
    UserAnswers()
      .set(SchemeNameId)(nonRacDACSchemeName).asOpt.get
      .set(RACDACNameId)(racDACSchemeName).asOpt.get
      .set(ContractOrPolicyNumberId)("dummy contract no").asOpt.get
      .set(DeclarationId)(true).asOpt.get
      .json.as[JsObject]


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
      FakeAllowAccessProvider(),
      controllerComponents,
      mockPensionAdminstratorConnector,
      view
    )

  def viewAsString(): String =
    view(email, racDACSchemeName)(fakeRequest, messages).toString

  appRunning()

  "SchemeSuccess Controller" must {

    "return OK and the correct view for a GET" in {
      when(mockUserAnswersCacheConnector.upsert(any(), any())(any(), any())).thenReturn(Future.successful(JsNull))
      when(mockPensionAdminstratorConnector.getPSAEmail(any(), any())).thenReturn(Future.successful(email))

      val result = controller().onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()

      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsValue])

      verify(mockUserAnswersCacheConnector, times(1)).upsert(any(), jsonCaptor.capture())(any(), any())
      val actualUserAnswers = UserAnswers(jsonCaptor.getValue)
      actualUserAnswers.get(RACDACNameId) mustBe None
      actualUserAnswers.get(ContractOrPolicyNumberId) mustBe None
      actualUserAnswers.get(DeclarationId) mustBe None
      actualUserAnswers.get(SchemeNameId) mustBe Some(nonRacDACSchemeName)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page for a POST" in {
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

    }
  }

}
