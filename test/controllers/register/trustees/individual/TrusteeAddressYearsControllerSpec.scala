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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.trustees.individual.{TrusteeAddressYearsId, TrusteeNameId}
import models.person.PersonName
import models.{AddressYears, EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.{JsError, JsResultException, JsSuccess}
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class TrusteeAddressYearsControllerSpec extends ControllerSpecBase {

  import TrusteeAddressYearsControllerSpec._

  "TrusteeAddressYearsController" must {

    "return OK and the correct view on a GET request" in {
      val result = controller(trusteeData).onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val answer = AddressYears.OverAYear
      val filledForm = form.fill(answer)
      assume(filledForm.errors.isEmpty)

      val result = controller(trusteeAndAnswerData(answer)).onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(filledForm)
    }

    "redirect to Session Expired on a GET request if no cached data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page when valid data is submitted" in {
      val answer = AddressYears.values.head
      val request = fakeRequest.withFormUrlEncodedBody(("value", answer.toString))

      val result = controller(trusteeData).onSubmit(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the answer when valid data is submitted" in {
      val answer = AddressYears.values.head
      val request = fakeRequest.withFormUrlEncodedBody(("value", answer.toString))

      val result = controller(trusteeData).onSubmit(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(request)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(TrusteeAddressYearsId(index), answer)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val filledForm = form.bind(Map.empty[String, String])
      assume(filledForm.errors.nonEmpty)

      val result = controller(trusteeData).onSubmit(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(filledForm)
    }

    "redirect to Session Expired on a POST request if no cached data exists" in {
      val result = controller(dontGetAnyData).onSubmit(mode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }

}

object TrusteeAddressYearsControllerSpec extends ControllerSpecBase {

  private val mode = NormalMode
  private val index = Index(0)

  private val trustee = PersonName(
    "Joe",
    "Bloggs"
  )

  private val form = new AddressYearsFormProvider()(Message("messages__trusteeAddressYears__error_required", trustee.fullName))

  private val onwardRoute = controllers.routes.IndexController.onPageLoad
  private val fakeNavigator = new FakeNavigator(onwardRoute)

  private val view = injector.instanceOf[addressYears]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new TrusteeAddressYearsController(
      frontendAppConfig,
      FakeUserAnswersService,
      fakeNavigator,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl(),
      controllerComponents,
      view
    )

  private val viewModel =
    AddressYearsViewModel(
      postCall = controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onSubmit(mode, Index(0), EmptyOptionalSchemeReferenceNumber),
      title = Message("messages__trusteeAddressYears__title", Message("messages__common__address_years__trustee").resolve),
      heading = Message("messages__trusteeAddressYears__heading", trustee.fullName),
      legend = Message("messages__trusteeAddressYears__title", trustee.fullName),
      subHeading = Some(Message(trustee.fullName))
    )

  private def viewAsString(form: Form[AddressYears] = form) =
    view(
      form,
      viewModel,
      None
    )(fakeRequest, messages).toString()

  private def trusteeUserAnswers: UserAnswers = {
    UserAnswers().set(TrusteeNameId(index))(trustee) match {
      case JsSuccess(userAnswers, _) => userAnswers
      case JsError(errors) => throw JsResultException(errors)
    }
  }

  private def trusteeData: DataRetrievalAction = {
    new FakeDataRetrievalAction(Some(trusteeUserAnswers.json))
  }

  private def trusteeAndAnswerData(answer: AddressYears): DataRetrievalAction = {
    trusteeUserAnswers.set(TrusteeAddressYearsId(index))(answer) match {
      case JsSuccess(userAnswers, _) => new FakeDataRetrievalAction(Some(userAnswers.json))
      case JsError(errors) => throw JsResultException(errors)
    }
  }

}
