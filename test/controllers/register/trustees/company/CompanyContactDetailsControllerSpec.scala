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

package controllers.register.trustees.company

import play.api.data.Form
import play.api.libs.json.Json
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import forms.register.trustees.company.CompanyContactDetailsFormProvider
import identifiers.register.trustees.company.CompanyContactDetailsId
import models.{CompanyDetails, Index, NormalMode}
import models.register.trustees.company.CompanyContactDetails
import views.html.register.trustees.company.companyContactDetails
import controllers.ControllerSpecBase
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.trustees.TrusteesId
import models.register.{SchemeDetails, SchemeType}

class CompanyContactDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new CompanyContactDetailsFormProvider()
  val form = formProvider()
  val firstIndex = Index(0)
  val invalidIndex = Index(10)
  val companyName = "test company name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany) :
  CompanyContactDetailsController  = new CompanyContactDetailsController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form) = companyContactDetails(frontendAppConfig, form, NormalMode, firstIndex, companyName)(fakeRequest, messages).toString

  val validData = Json.obj(
    SchemeDetailsId.toString->
    SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
    TrusteesId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
        CompanyDetails("test company name", Some("123456"), Some("abcd")),
        CompanyContactDetailsId.toString ->
        CompanyContactDetails("test@test.com", "123456789")
      ),
      Json.obj(
        CompanyDetailsId.toString ->
        CompanyDetails("test", Some("654321"), Some("bcda"))
      )
    )
  )

  "CompanyContactDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired from a GET when the index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(CompanyContactDetails("test@test.com", "123456789")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))

      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
