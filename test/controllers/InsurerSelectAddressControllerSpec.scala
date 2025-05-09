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

package controllers

import audit.testdoubles.StubSuccessfulAuditService
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.{InsuranceCompanyNameId, InsurerEnterPostCodeId, InsurerSelectAddressId}
import models.address.TolerantAddress
import models.{EmptyOptionalSchemeReferenceNumber, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{Enumerable, FakeNavigator, MapFormats}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class InsurerSelectAddressControllerSpec extends ControllerSpecBase with MockitoSugar with MapFormats with Enumerable.Implicits {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val fakeAuditService = new StubSuccessfulAuditService()
  val formProvider = new AddressListFormProvider()
  val schemeName = "ThisSchemeName"
  val insurerCompanyName = "blaaa ltd"
  private val schemeNameJsValue: JsObject = Json.obj("schemeName" -> schemeName,
    InsuranceCompanyNameId.toString -> insurerCompanyName)
  private val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )
  val addressObject: JsObject = Json.obj(InsurerEnterPostCodeId.toString -> addresses,
    InsuranceCompanyNameId.toString -> insurerCompanyName)

  val form: Form[?] = formProvider(Seq(0))

  private val view = injector.instanceOf[addressList]

  def controller(
                  dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs,
                  userAnswersService: UserAnswersService = FakeUserAnswersService
                ): InsurerSelectAddressController =
    new InsurerSelectAddressController(
      frontendAppConfig, messagesApi,
      userAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeAuditService,
      controllerComponents,
      view
    )

  def viewAsString(form: Form[?] = form, address: Seq[TolerantAddress] = addresses): String =
    view(
      form,
      AddressListViewModel(
        routes.InsurerSelectAddressController.onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber),
        routes.InsurerConfirmAddressController.onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber),
        addresses,
        heading = Message("messages__dynamic_whatIsAddress", insurerCompanyName),
        title = Message("messages__dynamic_whatIsAddress", Message("messages__theInsuranceCompany")),
        entityName = insurerCompanyName
      ),
      None
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

      val result = controller(dataRetrieval).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Address look up page" when {
      "no addresses are present after lookup" in {
        val dataRetrieval = new FakeDataRetrievalAction(Some(schemeNameJsValue))

        val result = controller(dataRetrieval).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.InsurerEnterPostcodeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber).url)
      }

      "no addresses are present after lookup (post)" in {

        val dataRetrieval = new FakeDataRetrievalAction(Some(schemeNameJsValue))

        val result = controller(dataRetrieval).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.InsurerEnterPostcodeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber).url)
      }
    }

    "redirect to the next page when valid data is submitted" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(addressObject))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))

      val result = controller(dataRetrieval).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "update the country of the chosen address to `GB`" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "0")

      val result = controller(new FakeDataRetrievalAction(Some(addressObject)), FakeUserAnswersService)
        .onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustEqual SEE_OTHER
      FakeUserAnswersService.userAnswer.get(InsurerSelectAddressId).value mustEqual (addresses.head.copy(countryOpt = Some("GB")))
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val dataRetrieval = new FakeDataRetrievalAction(Some(addressObject))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(dataRetrieval).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }

        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
      "scheme name is not present" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }
  }
}
