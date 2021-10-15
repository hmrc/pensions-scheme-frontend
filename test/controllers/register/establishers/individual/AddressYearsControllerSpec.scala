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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.individual.{AddressYearsId, EstablisherNameId}
import models.person.PersonName
import models.{AddressYears, Index, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService

import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

//scalastyle:off magic.number

class AddressYearsControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressYearsFormProvider()
  private val establisherName = "Test Name"

  private val form = formProvider(Message("messages__common_error__current_address_years", establisherName))
  private val firstIndex = Index(0)
  private val invalidIndex = Index(11)

  private val view = injector.instanceOf[addressYears]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher) =
    new AddressYearsController(
      frontendAppConfig,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      view,
      controllerComponents
    )

  private lazy val viewModel =
    AddressYearsViewModel(
      postCall = routes.AddressYearsController.onSubmit(NormalMode, firstIndex, None),
      title = Message("messages__establisher_address_years__title", Message("messages__theIndividual")),
      heading = Message("messages__establisher_address_years__title", establisherName),
      legend = Message("messages__establisher_address_years__title", establisherName),
      subHeading = Some(Message(establisherName))
    )

  private def viewAsString(form: Form[_] = form) =
    view(
      form,
      viewModel,
      None
    )(fakeRequest, messages).toString

  private val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        EstablisherNameId.toString ->
          PersonName("Test", "Name"),
        AddressYearsId.toString ->
          AddressYears.options.head.value.toString
      )
    )
  )

  "AddressYears Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustEqual OK
      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to session expired from a GET when the index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page when valid data is submitted with over_a_year" in {
      FakeUserAnswersService.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller().onSubmit(UpdateMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when valid data is submitted with under_a_year" in {
      FakeUserAnswersService.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.tail.head.value))
      val result = controller().onSubmit(UpdateMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
