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

package controllers.register.trustees.partnership

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipPreviousAddressPostcodeLookupId}
import models.address.TolerantAddress
import models.{Index, NormalMode, PartnershipDetails}
import org.mockito._
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

class PartnershipPreviousAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new PostCodeLookupFormProvider()
  val form: Form[String] = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  val index: Index = Index(0)
  val partnershipName: String = "test partnership name"

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private def fakeAddress(postCode: String) = TolerantAddress(
    Some("Address Line 1"),
    Some("Address Line 2"),
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    Some("GB")
  )

  private val testAnswer = "AB12 3CD"

  val validData: JsObject = Json.obj(
    TrusteesId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString ->
          PartnershipDetails("test partnership name"),
        PartnershipPreviousAddressPostcodeLookupId.toString ->
          Seq(fakeAddress(testAnswer))
      ),
      Json.obj(
        PartnershipDetailsId.toString ->
          PartnershipDetails("test")
      )
    )
  )
  private val view = injector.instanceOf[postcodeLookup]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteePartnership): PartnershipPreviousAddressPostcodeLookupController =
    new PartnershipPreviousAddressPostcodeLookupController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      fakeAddressLookupConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      PostcodeLookupViewModel(
        routes.PartnershipPreviousAddressPostcodeLookupController.onSubmit(NormalMode, index, None),
        routes.PartnershipPreviousAddressController.onPageLoad(NormalMode, index, None),
        Message("messages__partnershipPreviousAddressPostcodeLookup__title"),
        Message("messages__partnershipPreviousAddressPostcodeLookup__heading", partnershipName),
        Some(partnershipName)
      ),
      None
    )(fakeRequest, messages).toString

  "PartnershipPreviousAddressPostcodeLookup Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", testAnswer))
      when(fakeAddressLookupConnector.addressLookupByPostCode(ArgumentMatchers.eq(testAnswer))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Seq(fakeAddress(testAnswer))))
      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", ""))
      val boundForm = form.bind(Map("postcode" -> ""))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
