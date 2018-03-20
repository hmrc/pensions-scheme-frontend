/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import utils.{FakeNavigator, InputOption}
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import identifiers.register.establishers.individual.{AddressId, EstablisherDetailsId, PreviousAddressId}
import models.{Index, NormalMode}
import models.register.establishers.individual.EstablisherDetails
import views.html.register.establishers.individual.previousAddress
import controllers.ControllerSpecBase
import forms.address.AddressFormProvider
import identifiers.register.SchemeDetailsId
import models.addresslookup.Address
import models.register.{CountryOptions, SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import play.api.mvc.Call

class PreviousAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressFormProvider()
  val form = formProvider()
  val firstIndex = Index(0)
  val establisherName: String = "test first name test last name"
  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))
  def countryOptions: CountryOptions = new CountryOptions(options)

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): PreviousAddressController =
    new PreviousAddressController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider, countryOptions)

  def viewAsString(form: Form[_] = form): String = previousAddress(frontendAppConfig, form, NormalMode,
    firstIndex, options, establisherName)(fakeRequest, messages).toString

  val addressData = Address("address line 1", "address line 2", Some("test town"), Some("test county"), Some("test post code"), "GB")

  val validData: JsObject = Json.obj(SchemeDetailsId.toString -> Json.toJson(
    SchemeDetails("value 1", SchemeType.SingleTrust)),
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherDetailsId.toString ->
          EstablisherDetails("test first name", None, "test last name", LocalDate.now),
        PreviousAddressId.toString ->
          Json.toJson(Address("address line 1", "address line 2", Some("test town"),
            Some("test county"), Some("test post code"), "GB")
          ))))

  "PreviousAddress Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(addressData))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "value 1"),
        ("addressLine2", "value 2"), ("postCode.postCode", "AB1 1AB"), "country" -> "GB")

      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
