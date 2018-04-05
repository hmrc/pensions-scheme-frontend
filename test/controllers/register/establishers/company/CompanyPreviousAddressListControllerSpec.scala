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

package controllers.register.establishers.company

import play.api.data.Form
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import play.api.libs.json._
import identifiers.register.establishers.company.CompanyPreviousAddressPostcodeLookupId
import models.{CompanyDetails, Index, NormalMode}
import views.html.register.establishers.company.companyPreviousAddressList
import controllers.ControllerSpecBase
import forms.address.AddressListFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import models.address.Address
import models.register.{SchemeDetails, SchemeType}

class CompanyPreviousAddressListControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressListFormProvider()
  val form = formProvider(Seq(0, 1))
  val index = Index(0)
  val companyName = "test company name"
  val schemeName = "test scheme name"
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")

  val validData = Json.obj(
    SchemeDetailsId.toString ->
      SchemeDetails(schemeName, SchemeType.SingleTrust),
    EstablishersId.toString -> Json.arr(
      Json.obj(
    "companyDetails" -> CompanyDetails(companyName, None, None),
    CompanyPreviousAddressPostcodeLookupId.toString -> addresses
  )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany) =
    new CompanyPreviousAddressListController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form) = companyPreviousAddressList(frontendAppConfig, form, NormalMode, index, companyName, addresses)(fakeRequest, messages).toString

  "CompanyPreviousAddressList Controller" must {

    "return OK and the correct view for a GET" in {
      val getData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

   "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
      val getData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to postcode lookup when no address results exist (get)" in {
      val result = controller().onPageLoad(NormalMode, 0)(fakeRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, 0).url
    }

    "redirect to postcode lookup when no address results exist (post)" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
      val result = controller().onSubmit(NormalMode, 0)(postRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, 0).url
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val getData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
