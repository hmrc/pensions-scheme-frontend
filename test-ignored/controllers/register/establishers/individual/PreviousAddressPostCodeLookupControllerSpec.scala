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

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.{Index, NormalMode}
import org.mockito.Mockito._
import org.mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val firstIndex = Index(0)
  val establisherName: String = "Test Name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): PreviousAddressPostCodeLookupController =
    new PreviousAddressPostCodeLookupController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      fakeAddressLookupConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String = postcodeLookup(
    frontendAppConfig,
    form,
    PostcodeLookupViewModel(
      routes.PreviousAddressPostCodeLookupController.onSubmit(NormalMode, firstIndex, None),
      routes.PreviousAddressController.onPageLoad(NormalMode, firstIndex, None),
      Message("messages__establisher_individual_previous_address__heading", Message("messages__theIndividual").resolve),
      Message("messages__establisher_individual_previous_address__heading", establisherName),
      Some(establisherName)
    ),
    None
  )(fakeRequest, messages).toString


  "PreviousAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect a Bad Request when post code is not valid" in {
      val invalidPostCode = "invalid"
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", invalidPostCode))

      val boundForm = form.bindFromRequest()(postRequest)

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(invalidPostCode))(Matchers.any(), Matchers.any())).thenReturn(
        Future.successful(Seq(TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(invalidPostCode), Some("GB")))))

      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect a Bad Request when no results found for the input post code" in {
      val notFoundPostCode = "ZZ1 1ZZ"
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", notFoundPostCode))
      val boundForm = form.withError(FormError("value", "messages__error__postcode_no_results", Seq(notFoundPostCode)))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(notFoundPostCode))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Nil))

      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to the next page when valid data is submitted" in {
      val validPostCode = "ZZ1 1ZZ"
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", validPostCode))
      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(validPostCode))(Matchers.any(), Matchers.any())).thenReturn(
        Future.successful(Seq(TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostCode), Some("GB")))))

      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", ""))
      val boundForm = form.bind(Map("value" -> ""))
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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", "valid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
