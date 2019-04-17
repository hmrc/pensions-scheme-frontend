/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.vary

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.vary.AnyMoreChangesFormProvider
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import views.html.vary.anyMoreChanges


class AnyMoreChangesControllerSpec extends ControllerSpecBase {
  private val schemeName = Some("Test Scheme Name")
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  val formProvider = new AnyMoreChangesFormProvider()
  val form = formProvider()
  val date: String = LocalDate.now().plusDays(28).toString(DateTimeFormat.forPattern("dd MMMM YYYY"))
  private val postCall = controllers.vary.routes.AnyMoreChangesController.onSubmit _
  val srn = Some("123")

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs): AnyMoreChangesController =
    new AnyMoreChangesController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form) = anyMoreChanges(frontendAppConfig, form, schemeName, date, postCall(srn))(fakeRequest, messages).toString

  "AnyMoreChangesController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(srn)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(srn)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}
