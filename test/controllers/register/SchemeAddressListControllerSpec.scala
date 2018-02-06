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

package controllers.register

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.SchemeAddressListFormProvider
import identifiers.register.SchemePostCodeLookupId
import models.NormalMode
import models.addresslookup.Address
import models.register.SchemeDetails
import models.register.SchemeType.SingleTrust
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify}
import org.mockito.{Matchers, Mockito}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.schemeAddressList

class SchemeAddressListControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new SchemeAddressListFormProvider()
  val schemeName = "ThisSchemeName"
  val schemeDetails = Json.obj("schemeDetails" -> SchemeDetails(schemeName, SingleTrust))
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )
  val addressObject = Json.obj(SchemePostCodeLookupId.toString -> addresses)

  val dataCacheConnector = Mockito.spy(new FakeDataCacheConnector())

  val form = formProvider(Seq(0))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new SchemeAddressListController(
      frontendAppConfig,
      messagesApi,
      dataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form, address: Seq[Address] = addresses): String =
    schemeAddressList(frontendAppConfig, form, NormalMode, schemeName, addresses)(fakeRequest, messages).toString

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")

  "SchemeAddressList Controller" must {

    "return OK and the correct view for a GET" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(schemeDetails ++ addressObject))

      val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Address look up page" when {
      "no addresses are present after lookup" in {
        val dataRetrieval = new FakeDataRetrievalAction(Some(schemeDetails))

        val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.register.routes.SchemePostCodeLookupController.onPageLoad(NormalMode).url)
      }

      "no addresses are present after lookup (post)" in {

        val dataRetrieval = new FakeDataRetrievalAction(Some(schemeDetails))

        val result = controller(dataRetrieval).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.register.routes.SchemePostCodeLookupController.onPageLoad(NormalMode).url)
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(schemeDetails ++ addressObject))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))

      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "update the country of the chosen address to `GB`" in {

      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "0")

      controller(new FakeDataRetrievalAction(Some(schemeDetails ++ addressObject))).onSubmit(NormalMode)(postRequest)

      verify(dataCacheConnector, times(1)).save(any(), any(), Matchers.eq(addresses.head.copy(country = "GB")))(any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(schemeDetails ++ addressObject))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
      "scheme name is not present" in {
        val result = controller(getEmptyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

  }
}
