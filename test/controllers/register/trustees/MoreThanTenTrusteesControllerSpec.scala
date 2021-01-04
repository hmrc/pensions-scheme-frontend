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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.MoreThanTenTrusteesFormProvider
import identifiers.register.trustees.MoreThanTenTrusteesId
import models.NormalMode
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import views.html.register.trustees.moreThanTenTrustees

class MoreThanTenTrusteesControllerSpec extends ControllerSpecBase {
  appRunning()
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new MoreThanTenTrusteesFormProvider()
  val form: Form[Boolean] = formProvider()
  val schemeName = "Test Scheme Name"
  val validData: JsObject = Json.obj(
    MoreThanTenTrusteesId.toString -> false
  )


  private val view = injector.instanceOf[moreThanTenTrustees]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): MoreThanTenTrusteesController =
    new MoreThanTenTrusteesController(frontendAppConfig, messagesApi, FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider,
      stubMessagesControllerComponents(),
      view)

  val submitUrl: Call = controllers.register.trustees.routes.MoreThanTenTrusteesController.onSubmit(NormalMode, None)

  def viewAsString(form: Form[_] = form): String = view(form, NormalMode, None, submitUrl, None)(fakeRequest, messages).toString

  "MoreThanTenTrustees Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(false))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
