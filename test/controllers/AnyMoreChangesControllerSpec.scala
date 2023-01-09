/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import controllers.actions._
import forms.AnyMoreChangesFormProvider
import play.api.data.Form
import play.api.test.Helpers._

import utils.FakeNavigator
import views.html.anyMoreChanges


class AnyMoreChangesControllerSpec extends ControllerSpecBase {
  private val schemeName = Some("Test Scheme Name")
  private def onwardRoute = controllers.routes.IndexController.onPageLoad
  val formProvider = new AnyMoreChangesFormProvider()
  val form = formProvider()
  def date: String = LocalDate.now().plusDays(28).format(DateTimeFormatter.ofPattern("d MMMM YYYY"))
  private val postCall = controllers.routes.AnyMoreChangesController.onSubmit _
  val srn = Some("123")

  def dateToCompleteDeclaration: String = LocalDate.now().plusDays(28).
    format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
  private val view = injector.instanceOf[anyMoreChanges]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs): AnyMoreChangesController =
    new AnyMoreChangesController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, schemeName, dateToCompleteDeclaration,
    postCall(srn), srn)(fakeRequest, messages).toString

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
