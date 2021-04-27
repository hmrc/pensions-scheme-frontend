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

import connectors.{PensionAdministratorConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.racdac.RACDACNameId
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.mvc.Results._
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
  private val schemeName = "schemeName"

  val submissionReferenceNumber = "XX123456789132"

  private val fakeUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val mockPensionAdminstratorConnector = mock[PensionAdministratorConnector]
  val validData: JsObject = UserAnswers().set(RACDACNameId)(schemeName).asOpt.get.json.as[JsObject]
  private val view = injector.instanceOf[schemeSuccess]

  private def controller(dataRetrievalAction: DataRetrievalAction =
                         new FakeDataRetrievalAction(Some(validData))): SchemeSuccessController =
    new SchemeSuccessController(
      frontendAppConfig,
      messagesApi,
      fakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeAllowAccessProvider(),
      controllerComponents,
      mockPensionAdminstratorConnector,
      view
    )

  def viewAsString(): String =
    view(email,schemeName)(fakeRequest, messages).toString

  appRunning()

  "SchemeSuccess Controller" must {

    "return OK and the correct view for a GET" in {
      when(fakeUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      when(mockPensionAdminstratorConnector.getPSAEmail(any(),any())).thenReturn(Future.successful(email))

      val result = controller().onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(fakeUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page for a POST" in {
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

    }
  }

}
