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
import connectors.{FakeUserAnswersCacheConnector, PensionAdministratorConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.racdac.RACDACContractOrPolicyNumberFormProvider
import identifiers.racdac.{RACDACContractOrPolicyNumberId, RACDACNameId}
import models.NormalMode
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.racdac.racDACContractOrPolicyNumber

import scala.concurrent.Future

class RACDACContractOrPolicyNumberControllerSpec extends ControllerSpecBase with MockitoSugar {
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val psaName = "Mr Maxwell"
  val formProvider = new RACDACContractOrPolicyNumberFormProvider()
  val form: Form[String] = formProvider()

  val config: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  val pensionAdministratorConnector: PensionAdministratorConnector = injector.instanceOf[PensionAdministratorConnector]
  val mockPensionAdministratorConnector: PensionAdministratorConnector = mock[PensionAdministratorConnector]

  private val view = injector.instanceOf[racDACContractOrPolicyNumber]

  private val racdacName = "racdac scheme"
  private val uaWithRACDACName = UserAnswers().set(RACDACNameId)(racdacName).asOpt.get

  private def getMandatorySchemeName: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(uaWithRACDACName.json)
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName): RACDACContractOrPolicyNumberController =
    new RACDACContractOrPolicyNumberController(
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl(),
      formProvider,
      mockPensionAdministratorConnector,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, NormalMode, psaName, racdacName)(fakeRequest, messages).toString

  "RACDACContractOrPolicyNumber Controller" must {

    "return OK and the correct view for a GET" in {
      when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
      val validData =Json.obj("racdac" -> Json.obj(
        RACDACNameId.toString -> racdacName,
        RACDACContractOrPolicyNumberId.toString -> "value 1")
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill("value 1"))
    }

    "redirect to the next page when valid data is submitted" in {
      when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("racDACContractOrPolicyNumber", "value 1"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors" when {
      "invalid data is submitted" in {
        when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
        val postRequest = fakeRequest.withFormUrlEncodedBody(("racDACContractOrPolicyNumber", ""))
        val boundForm = form.bind(Map("racDACContractOrPolicyNumber" -> ""))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }

  }
}
