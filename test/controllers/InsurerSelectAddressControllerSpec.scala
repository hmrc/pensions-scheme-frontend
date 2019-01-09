/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.{InsurerEnterPostCodeId, InsurerSelectAddressId}
import models.NormalMode
import models.address.TolerantAddress
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{Enumerable, FakeNavigator, MapFormats}
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class InsurerSelectAddressControllerSpec extends ControllerSpecBase with MockitoSugar with MapFormats with Enumerable.Implicits {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressListFormProvider()
  val schemeName = "ThisSchemeName"
  val schemeNameJsValue: JsObject = Json.obj("schemeName" -> schemeName)
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )
  val addressObject: JsObject = Json.obj(InsurerEnterPostCodeId.toString -> addresses)

  val form = formProvider(Seq(0))

  def controller(
                  dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs,
                  dataCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
                ): InsurerSelectAddressController =
    new InsurerSelectAddressController(
      frontendAppConfig, messagesApi,
      dataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  def viewAsString(form: Form[_] = form, address: Seq[TolerantAddress] = addresses): String =
    addressList(
      frontendAppConfig,
      form,
      AddressListViewModel(
        routes.InsurerSelectAddressController.onSubmit(NormalMode),
        routes.InsurerConfirmAddressController.onPageLoad(NormalMode),
        addresses,
        subHeading = Some(schemeName)
      )
    )(fakeRequest, messages).toString

  def address(postCode: String): TolerantAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some(postCode),
    Some("United Kingdom")
  )

  "InsurerSelectAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(addressObject))

      val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Address look up page" when {
      "no addresses are present after lookup" in {
        val dataRetrieval = new FakeDataRetrievalAction(Some(schemeNameJsValue))

        val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.InsurerEnterPostcodeController.onPageLoad(NormalMode).url)
      }

      "no addresses are present after lookup (post)" in {

        val dataRetrieval = new FakeDataRetrievalAction(Some(schemeNameJsValue))

        val result = controller(dataRetrieval).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.InsurerEnterPostcodeController.onPageLoad(NormalMode).url)
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(addressObject))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))

      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "update the country of the chosen address to `GB`" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "0")

      val result = controller(new FakeDataRetrievalAction(Some(addressObject)), FakeUserAnswersCacheConnector)
        .onSubmit(NormalMode)(postRequest)

      status(result) mustEqual SEE_OTHER
      FakeUserAnswersCacheConnector.verify(InsurerSelectAddressId, addresses.head.copy(country = Some("GB")))
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(addressObject))

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
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
