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

package controllers.behaviours

import java.time.LocalDate

import controllers.ControllerSpecBase
import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import forms.DOBFormProvider
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.JsObject
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.html.register.DOB

import scala.concurrent.Future

trait DateOfBirthControllerBehaviours extends ControllerSpecBase
  with MockitoSugar
  with ScalaFutures {

  val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider: DOBFormProvider = new DOBFormProvider
  val form: Form[LocalDate] = formProvider()
  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthValue
  val year: Int = LocalDate.now().getYear - 20
  val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    fakeRequest.withFormUrlEncodedBody(("date.day", day.toString), ("date.month", month.toString), ("date.year", year.toString))

  private val view = injector.instanceOf[DOB]

  def viewAsString(form: Form[LocalDate], mode: Mode, fullName: String, viewModel: DateOfBirthViewModel): String =
    view(form, mode, None, fullName, viewModel)(fakeRequest, messages).toString

  def dateOfBirthController(get: DataRetrievalAction => Action[AnyContent],
                            post: DataRetrievalAction => Action[AnyContent],
                            viewModel: DateOfBirthViewModel,
                            mode: Mode,
                            validData: JsObject,
                            requiredData: FakeDataRetrievalAction,
                            fullName: String): Unit = {

    "DateOfBirthController" must {

      "return OK and the correct view for a GET" in {
        val result: Future[Result] = get(requiredData)(fakeRequest)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(form, mode, fullName, viewModel)
      }

      "populate the view correctly on a GET when the question has previously been answered" in {
        val result: Future[Result] = get(new FakeDataRetrievalAction(Some(validData)))(fakeRequest)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(form.fill(LocalDate.of(year, month, day)), mode, fullName, viewModel)
      }

      "redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersService.save(any(), any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(validData))

        val result: Future[Result] = post(requiredData)(postRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "return a Bad Request and errors when invalid data is submitted" in {
        val result: Future[Result] = post(requiredData)(fakeRequest.withFormUrlEncodedBody(("value", "invalid value")))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        status(result) mustBe BAD_REQUEST

        contentAsString(result) mustBe viewAsString(boundForm, mode, fullName, viewModel)
      }

      "redirect to Session Expired for a GET if no existing data is found" in {
        val result: Future[Result] = get(dontGetAnyData)(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }

      "redirect to Session Expired for a POST if no existing data is found" in {
        val result: Future[Result] = post(dontGetAnyData)(postRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }
  }
}
