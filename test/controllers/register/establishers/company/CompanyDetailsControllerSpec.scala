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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.CompanyDetailsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import models._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService

import utils.FakeNavigator
import views.html.register.establishers.company.companyDetails

class CompanyDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new CompanyDetailsFormProvider()
  val form = formProvider()
  val firstIndex = Index(0)
  val invalidIndex = Index(3)
  val schemeName = "Test Scheme Name"
  val postCall = routes.CompanyDetailsController.onSubmit _

  private val view = injector.instanceOf[companyDetails]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CompanyDetailsController =
    new CompanyDetailsController(frontendAppConfig, messagesApi, FakeUserAnswersService, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider, controllerComponents, view)

  def viewAsString(form: Form[_] = form): String = view(form, NormalMode, firstIndex, None,
    postCall(NormalMode, None, 0), None)(fakeRequest, messages).toString

  private val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test company name")
      )
    )
  )

  "CompanyDetails Controller" must {

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode, None, firstIndex)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, None, firstIndex)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(CompanyDetails("test company name")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("companyName", "test company name"), ("vatNumber", "GB123456789"), ("payeNumber", "1234567824"))
      val result = controller().onSubmit(NormalMode, None, firstIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, None, firstIndex)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None, firstIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, None, firstIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
