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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.partnership.{PartnershipAddressYearsId, PartnershipDetailsId}
import models._
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import services.FakeUserAnswersService

import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class PartnershipAddressYearsControllerSpec extends ControllerSpecBase {

  import PartnershipAddressYearsControllerSpec._

  "TrusteeAddressYearsController" must {

    "return OK and the correct view on a GET request" in {
      val result = controller(trusteeData).onPageLoad(mode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val answer = AddressYears.OverAYear
      val filledForm = form.fill(answer)
      assume(filledForm.errors.isEmpty)

      val result = controller(partnershipAndAnswerData(answer)).onPageLoad(mode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(filledForm)
    }

    "redirect to Session Expired on a GET request if no cached data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(mode, index, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page when valid data is submitted" in {
      val answer = AddressYears.values.head
      val request = fakeRequest.withFormUrlEncodedBody(("value", answer.toString))

      val result = controller(trusteeData).onSubmit(mode, index, None)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the answer when valid data is submitted" in {
      val answer = AddressYears.values.head
      val request = fakeRequest.withFormUrlEncodedBody(("value", answer.toString))

      val result = controller(trusteeData).onSubmit(mode, index, None)(request)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(PartnershipAddressYearsId(index), answer)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val filledForm = form.bind(Map.empty[String, String])
      assume(filledForm.errors.nonEmpty)

      val result = controller(trusteeData).onSubmit(mode, index, None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(filledForm)
    }

    "redirect to Session Expired on a POST request if no cached data exists" in {
      val result = controller(dontGetAnyData).onSubmit(mode, index, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }

}

object PartnershipAddressYearsControllerSpec extends ControllerSpecBase {

  private val mode = NormalMode
  private val index = Index(0)

  private val partnership = PartnershipDetails(
    "test partnership name"
  )

  private val form = new AddressYearsFormProvider()(Message("messages__partnershipAddressYears__error", partnershipUserAnswers.get(PartnershipDetailsId(index)).get.name))

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(onwardRoute)

  private val view = injector.instanceOf[addressYears]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new PartnershipAddressYearsController(
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
      postCall = routes.PartnershipAddressYearsController.onSubmit(mode, index, None),
      title = Message("messages__partnershipAddressYears__title", Message("messages__thePartnership").resolve),
      heading = Message("messages__partnershipAddressYears__heading", partnershipUserAnswers.get(PartnershipDetailsId(index)).get.name),
      legend = Message("messages__partnershipAddressYears__heading", partnershipUserAnswers.get(PartnershipDetailsId(index)).get.name),
      subHeading = Some(Message(partnership.name))
    )

  private def viewAsString(form: Form[AddressYears] = form) =
    view(
      form,
      viewModel,
      None
    )(fakeRequest, messages).toString()

  private def partnershipUserAnswers: UserAnswers = {
    UserAnswers().set(PartnershipDetailsId(index))(partnership) match {
      case JsSuccess(userAnswers, _) => userAnswers
      case JsError(errors) => throw JsResultException(errors)
    }
  }

  private def trusteeData: DataRetrievalAction = {
    new FakeDataRetrievalAction(Some(partnershipUserAnswers.json))
  }

  private def partnershipAndAnswerData(answer: AddressYears): DataRetrievalAction = {
    partnershipUserAnswers.set(PartnershipAddressYearsId(index))(answer) match {
      case JsSuccess(userAnswers, _) => new FakeDataRetrievalAction(Some(userAnswers.json))
      case JsError(errors) => throw JsResultException(errors)
    }
  }

}
