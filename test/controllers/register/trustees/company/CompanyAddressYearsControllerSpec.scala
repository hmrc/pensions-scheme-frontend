/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.{CompanyAddressYearsId, CompanyDetailsId}
import models.{AddressYears, CompanyDetails, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService

import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

//scalastyle:off magic.number

class CompanyAddressYearsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new forms.address.AddressYearsFormProvider()
  val companyDetails = CompanyDetails("companyName")
  val form = formProvider(Message("messages__common_error__current_address_years", companyDetails.companyName))
  val firstIndex = Index(0)
  val invalidIndex = Index(10)
  val questionTextTitle = "messages__company_trustee_address_years__title"
  val questionTextHeading = "messages__company_trustee_address_years__heading"
  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(CompanyDetailsId.toString -> companyDetails))
  )))

  val viewmodel = AddressYearsViewModel(
    postCall = routes.CompanyAddressYearsController.onSubmit(NormalMode, firstIndex, None),
    title = Message(questionTextTitle),
    heading = Message(questionTextHeading, companyDetails.companyName),
    legend = Message("messages__company_address_years__title"),
    Some(companyDetails.companyName)
  )

  private val view = injector.instanceOf[addressYears]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CompanyAddressYearsController =
    new CompanyAddressYearsController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeUserAnswersService, FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider,
      controllerComponents,
      view)

  def viewAsString(form: Form[_] = form): String = view(form, viewmodel, None)(fakeRequest, messages).toString

  val validData: JsResult[UserAnswers] = UserAnswers()
    .set(CompanyDetailsId(0))(companyDetails)

  val getRelevantData = new FakeDataRetrievalAction(Some(validData.get.json))

  "AddressYears Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getData = new FakeDataRetrievalAction(Some(validData.flatMap(_.set(CompanyAddressYearsId(0))(AddressYears.values.head)).get.json))
      val result = controller(getData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to session expired from a GET when the index is invalid" in {
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(getRelevantData).onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller(getRelevantData).onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
