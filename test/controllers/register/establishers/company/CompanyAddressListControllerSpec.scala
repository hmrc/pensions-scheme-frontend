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

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.individual.AddressListFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPostCodeLookupId}
import models.address.Address
import models.{CompanyDetails, NormalMode}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.register.establishers.company.companyAddressList

class CompanyAddressListControllerSpec extends ControllerSpecBase with OptionValues {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressListFormProvider()
  val form = formProvider(Seq(0, 1))
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CompanyAddressListController =
    new CompanyAddressListController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String = companyAddressList(
    frontendAppConfig,
    form,
    NormalMode,
    0,
    addresses,
    "Company Name"
  )(fakeRequest, messages).toString

  val validData = UserAnswers(Json.obj())
    .set(CompanyDetailsId(0))(CompanyDetails("Company Name", None, None))
    .flatMap(_.set(CompanyPostCodeLookupId(0))(addresses))

  val getValidData = new FakeDataRetrievalAction(validData.asOpt.map(_.json))

  val getDataWithoutName = new FakeDataRetrievalAction(
    validData
      .flatMap(_.remove(CompanyDetailsId(0)))
      .asOpt.map(_.json)
  )

  val getDataWithNoAddresses = new FakeDataRetrievalAction(
    validData
      .flatMap(_.remove(CompanyPostCodeLookupId(0)))
      .asOpt.map(_.json)
  )

  "CompanyAddressList Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getValidData).onPageLoad(NormalMode, 0)(fakeRequest)
      status(result) mustEqual OK
      contentAsString(result) mustEqual viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
      val result = controller(getValidData).onSubmit(NormalMode, 0)(postRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "redirect to postcode lookup when no address results exist (get)" in {
      val result = controller(getDataWithNoAddresses).onPageLoad(NormalMode, 0)(fakeRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, 0).url
    }

    "redirect to postcode lookup when no address results exist (post)" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
      val result = controller(getDataWithNoAddresses).onSubmit(NormalMode, 0)(postRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, 0).url
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller(getValidData).onSubmit(NormalMode, 0)(postRequest)
      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, 0)(fakeRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, 0)(postRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
