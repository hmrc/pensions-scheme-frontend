/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import audit.UserResearchEvent
import audit.testdoubles.StubSuccessfulAuditService
import connectors.FakeUserAnswersCacheConnector
import controllers.actions._
import forms.UserResearchDetailsFormProvider
import identifiers.UserResearchDetailsId
import models.UserResearchDetails
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.userResearchDetails

class UserResearchDetailsControllerSpec extends ControllerSpecBase with ScalaFutures {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new UserResearchDetailsFormProvider()
  val form = formProvider()
  val fakeAuditService = new StubSuccessfulAuditService()
  val name = "test name"
  val email = "test@test.com"


  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): UserResearchDetailsController =
    new UserResearchDetailsController(frontendAppConfig, messagesApi, FakeUserAnswersCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider, fakeAuditService)

  def viewAsString(form: Form[_] = form): String = userResearchDetails(frontendAppConfig, form)(fakeRequest, messages).toString

  "UserResearchContactDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("name", "testName"), ("email", "test@email.com"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "send User Research Audit event when valid data is submitted" in {

      val validData = Json.obj(UserResearchDetailsId.toString -> UserResearchDetails(name, email))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("name", name), ("email", email))

      fakeAuditService.reset()

      val result = controller(getRelevantData).onSubmit()(postRequest)


      whenReady(result) {
        _ =>
          fakeAuditService.verifySent(
            UserResearchEvent(
              FakeAuthAction.externalId,
              "test name",
              "test@test.com"
            )
          )
      }
    }
  }
}
