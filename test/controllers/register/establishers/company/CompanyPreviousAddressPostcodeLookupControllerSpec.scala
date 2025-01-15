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

package controllers.register.establishers.company

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPreviousAddressPostcodeLookupId}
import models.address.TolerantAddress
import models.{CompanyDetails, EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import org.mockito._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
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

import scala.concurrent.Future

class CompanyPreviousAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()

  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  val index = Index(0)
  val companyName: String = "test company name"

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
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test company name"),
        CompanyPreviousAddressPostcodeLookupId.toString ->
          Seq(fakeAddress(testAnswer))
      ),
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test")
      )
    )
  )

  private val view = injector.instanceOf[postcodeLookup]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): CompanyPreviousAddressPostcodeLookupController =
    new CompanyPreviousAddressPostcodeLookupController(
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
      view,
      controllerComponents
    )

  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      PostcodeLookupViewModel(
        routes.CompanyPreviousAddressPostcodeLookupController.onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index),
        routes.CompanyPreviousAddressController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index),
        Message("messages__establisherPreviousPostCode__title"),
        Message("messages__establisherPreviousPostCode__h1", companyName),
        Some(companyName)
      ),
      None
    )(fakeRequest, messages).toString

  "CompanyPreviousAddressPostcodeLookup Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("postcode", testAnswer))
      when(fakeAddressLookupConnector.addressLookupByPostCode(ArgumentMatchers.eq(testAnswer))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Seq(fakeAddress(testAnswer))))
      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
