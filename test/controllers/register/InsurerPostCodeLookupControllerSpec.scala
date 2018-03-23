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

import connectors.{AddressLookupConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.individual.PostCodeLookupFormProvider
import models.address.{Address, AddressRecord}
import models.NormalMode
import org.mockito.Mockito._
import org.mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import views.html.register.insurerPostCodeLookup

import scala.concurrent.Future

class InsurerPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  val schemeName: String = "Test Scheme Name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): InsurerPostCodeLookupController =
    new InsurerPostCodeLookupController(frontendAppConfig, messagesApi, FakeDataCacheConnector, fakeAddressLookupConnector,
      new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = insurerPostCodeLookup(frontendAppConfig, form, NormalMode,
    schemeName)(fakeRequest, messages).toString

  "Scheme Address Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect a Bad Request when post code is not valid" in {
      val invalidPostCode = "invalid"
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", invalidPostCode))
      val boundForm = form.withError(FormError("value", "messages__error__postcode_invalid"))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(invalidPostCode))(Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect a Bad Request when no results found for the input post code" in {
      val notFoundPostCode = "noResult"
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", notFoundPostCode))
      val boundForm = form.withError(FormError("value", "messages__error__postcode_no_results"))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(notFoundPostCode))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(Nil)))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to the next page when valid data is submitted" in {
      val validPostCode = "valid"
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", validPostCode))
      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostCode))(Matchers.any(), Matchers.any())).thenReturn(
        Future.successful(Some(Seq(AddressRecord(Address("address line 1", "address line 2", None, None, Some(validPostCode), "GB"))))))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "valid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
