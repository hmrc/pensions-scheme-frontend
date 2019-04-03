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

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyContactDetailsId
import models._
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.{ContactDetailsViewModel, Message}
import views.html.contactDetails

//scalastyle:off magic.number

class CompanyContactDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new ContactDetailsFormProvider()
  val form = formProvider()
  val firstIndex = Index(0)
  val invalidIndex = Index(10)
  val companyName = "test company name"

  private def viewmodel(mode: Mode, index: Index, companyName: String) = ContactDetailsViewModel(
    postCall = routes.CompanyContactDetailsController.onSubmit(mode, index, None),
    title = Message("messages__trustee_company_contact_details__title"),
    heading = Message("messages__trustee_company_contact_details__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(companyName)
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany):
  CompanyContactDetailsController =
    new CompanyContactDetailsController(
      new FakeNavigator(desiredRoute = onwardRoute),
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String =
    contactDetails(
      frontendAppConfig,
      form,
      viewmodel(NormalMode, firstIndex, companyName),

      None
    )(fakeRequest, messages).toString

  val validData: JsObject = Json.obj(
    TrusteesId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test company name"),
        CompanyContactDetailsId.toString ->
          ContactDetails("test@test.com", "123456789")
      ),
      Json.obj(
        CompanyDetailsId.toString ->
          CompanyDetails("test")
      )
    )
  )

  "CompanyContactDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired from a GET when the index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(ContactDetails("test@test.com", "123456789")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))

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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
