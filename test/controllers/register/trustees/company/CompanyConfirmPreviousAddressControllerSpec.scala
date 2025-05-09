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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.ConfirmAddressFormProvider
import identifiers.register.trustees.ExistingCurrentAddressId
import identifiers.register.trustees.company.{CompanyConfirmPreviousAddressId, CompanyDetailsId}
import models._
import models.address.Address
import play.api.data.Form
import play.api.libs.json.JsResult
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{CountryOptions, FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

class CompanyConfirmPreviousAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val companyName: String = "test-company-name"

  val formProvider = new ConfirmAddressFormProvider()
  val form: Form[Boolean] = formProvider(Message("confirmPreviousAddress.error", companyName))

  val testAddress = Address(
    "address line 1",
    "address line 2",
    Some("test town"),
    Some("test county"),
    Some("test post code"), "GB"
  )

  private def viewmodel = ConfirmAddressViewModel(
    postCall = routes.CompanyConfirmPreviousAddressController.onSubmit(Index(0), OptionalSchemeReferenceNumber(srn)),
    title = Message("messages__confirmPreviousAddress__title"),
    heading = Message("messages__confirmPreviousAddress__heading", companyName),
    hint = None,
    address = testAddress,
    name = companyName,
    srn = OptionalSchemeReferenceNumber(srn)
  )

  val countryOptions = new CountryOptions(environment, frontendAppConfig)
  val schemeName = "Test Scheme Name"
  val index = 0
  val srn = Some(SchemeReferenceNumber("srn"))

  private val view = injector.instanceOf[confirmPreviousAddress]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new CompanyConfirmPreviousAddressController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      countryOptions,
      controllerComponents,
      view
    )

  def viewAsString(form: Form[?] = form): String =
    view(
      form,
      viewmodel,
      countryOptions,
      None
    )(fakeRequest, messages).toString

  val validData: JsResult[UserAnswers] = UserAnswers()
    .set(CompanyDetailsId(index))(CompanyDetails(companyName)).flatMap(_.set(
    ExistingCurrentAddressId(index))(testAddress))

  val getRelevantData = new FakeDataRetrievalAction(Some(validData.get.json))

  "CompanyConfirmPreviousAddressController" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getData = new FakeDataRetrievalAction(Some(validData.flatMap(_.set(CompanyConfirmPreviousAddressId(index))(false)).get.json))

      val result = controller(getData).onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(false))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(getRelevantData).onSubmit(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}
