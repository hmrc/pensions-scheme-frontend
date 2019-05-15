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

package controllers.register.trustees.company

import services.FakeUserAnswersService
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.company.CompanyUniqueTaxReferenceFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyUniqueTaxReferenceId}
import models.{UniqueTaxReference, _}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.trustees.company.companyUniqueTaxReference

//scalastyle:off magic.number

class CompanyUniqueTaxReferenceControllerSpec extends ControllerSpecBase {
  appRunning()
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val firstIndex = Index(0)
  val invalidIndex = Index(12)
  val formProvider = new CompanyUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()
  val companyName = "test company name"

  val validData: JsObject = Json.obj(
    TrusteesId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test company name"),
        CompanyUniqueTaxReferenceId.toString ->
          UniqueTaxReference.Yes("1234567891")
      ),
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test")
      )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany): CompanyUniqueTaxReferenceController =
    new CompanyUniqueTaxReferenceController(frontendAppConfig, messagesApi, FakeUserAnswersService, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider)
  val submitUrl = controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onSubmit(NormalMode, firstIndex, None)

  def viewAsString(form: Form[_] = form): String = companyUniqueTaxReference(frontendAppConfig, form, NormalMode, firstIndex, None, submitUrl, None)(fakeRequest, messages).toString

  "CompanyUniqueTaxReference Controller" must {

    "return OK and the correct view for a GET when company name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to Session Expired page when the index is not valid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("uniqueTaxReference.hasUtr", "true"), ("uniqueTaxReference.utr", "1234565656"))
      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
