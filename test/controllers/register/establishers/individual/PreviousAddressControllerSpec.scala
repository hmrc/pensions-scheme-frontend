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

package controllers.register.establishers.individual

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.individual.{EstablisherNameId, PreviousAddressId}
import models.address.{Address, TolerantAddress}
import models.person.PersonName
import models.{Index, NormalMode}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, InputOption, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class PreviousAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val form = formProvider()
  val firstIndex = Index(0)
  val establisherName: String = "Test Name"
  val heading: Message = "messages__common__confirmPreviousAddress__h1"

  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))

  def countryOptions: CountryOptions = new CountryOptions(options)

  val fakeAuditService = new StubSuccessfulAuditService()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): PreviousAddressController =
    new PreviousAddressController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      fakeAuditService
    )

  def viewAsString(form: Form[_] = form): String =
    manualAddress(
      frontendAppConfig,
      form,
      ManualAddressViewModel(
        postCall = routes.PreviousAddressController.onSubmit(NormalMode, firstIndex, None),
        countryOptions = countryOptions.options,
        title = Message(heading, Message("messages__theIndividual").resolve),
        heading = Message(heading,establisherName)
      ),
      None
    )(fakeRequest, messages).toString

  val addressData = Address("address line 1", "address line 2", Some("test town"), Some("test county"), Some("test post code"), "GB")

  val validData: JsObject = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherNameId.toString ->
          PersonName("Test", "Name"),
        PreviousAddressId.toString ->
          Json.toJson(Address("address line 1", "address line 2", Some("test town"),
            Some("test county"), Some("test post code"), "GB")
          ))))

  "PreviousAddress Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(addressData))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "value 1"),
        ("addressLine2", "value 2"), ("postCode", "AB1 1AB"), "country" -> "GB")

      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

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
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "send an audit event when valid data is submitted" in {

      val existingAddress = Address(
        "existing-line-1",
        "existing-line-2",
        None,
        None,
        None,
        "existing-country"
      )

      val selectedAddress = TolerantAddress(None, None, None, None, None, None)

      val data =
        UserAnswers()
          .establishersIndividualPreviousAddress(firstIndex, existingAddress)
          .establishersIndividualPreviousAddressList(firstIndex, selectedAddress)
          .dataRetrievalAction

      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      fakeAuditService.reset()

      val result = controller(data).onSubmit(NormalMode, firstIndex, None)(postRequest)

      whenReady(result) {
        _ =>
          fakeAuditService.verifySent(
            AddressEvent(
              FakeAuthAction.externalId,
              AddressAction.LookupChanged,
              s"Establisher Individual Previous Address: $establisherName",
              Address(
                "value 1",
                "value 2",
                None,
                None,
                Some("NE1 1NE"),
                "GB"
              )
            )
          )
      }
    }
  }
}
