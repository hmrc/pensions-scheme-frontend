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
import forms.register.establishers.company.AddressYearsFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyAddressId, CompanyAddressYearsId, CompanyDetailsId}
import models.address.Address
import models.register.{SchemeDetails, SchemeType}
import models.{AddressYears, CompanyDetails, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears
import views.html.register.establishers.company.companyAddressYears

class CompanyAddressYearsControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressYearsFormProvider()
  private val form = formProvider()
  private val firstIndex = Index(0)
  private val invalidIndex = Index(10)
  private val companyName = "test company"
  private val address = Address("value 1", "value 2", None, None, None, "GB")

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CompanyAddressYearsController =
    new CompanyAddressYearsController(
      frontendAppConfig,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private def viewAsString(form: Form[_] = form) =
    addressYears(
      frontendAppConfig,
      form,
      AddressYearsViewModel(
        routes.CompanyAddressYearsController.onSubmit(NormalMode, firstIndex),
        Message("messages__company_address_years__title"),
        Message("messages__company_address_years__title"),
        Message("messages__company_address_years__title"),
        Some(companyName)
      )
    )(fakeRequest, messages).toString

  val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName, Some("123456"), Some("abcd"))
      )
    )
  )

  private val getRelevantData = new FakeDataRetrievalAction(Some(validData))

  "CompanyAddressYears Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val previousAnsweredValidData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(companyName, Some("123456"), Some("abcd")),
            CompanyAddressYearsId.toString -> AddressYears.options.head.value.toString
          )
        )
      )

      val getRelevantData = new FakeDataRetrievalAction(Some(previousAnsweredValidData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to session expired from a GET when the index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(getRelevantData).onSubmit(NormalMode, firstIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller(getRelevantData).onSubmit(NormalMode, firstIndex)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
