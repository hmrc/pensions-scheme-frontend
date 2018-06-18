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

package controllers.register.establishers.individual

import play.api.data.Form
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeNavigator, FakeNavigator2}
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import identifiers.register.establishers.individual.{ContactDetailsId, EstablisherDetailsId}
import models._
import views.html.register.establishers.individual.contactDetails
import controllers.ControllerSpecBase
import forms.ContactDetailsFormProvider
import identifiers.register.SchemeDetailsId
import models.register.{SchemeDetails, SchemeType}
import models.register.establishers.individual.{EstablisherDetails, EstablishersIndividualMap}
import org.joda.time.LocalDate
import play.api.mvc.Call

class ContactDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new ContactDetailsFormProvider()
  val form = formProvider()
  val firstIndex = Index(0)
  val invalidIndex = Index(10)
  val establisherName = "test first name test last name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): ContactDetailsController =
    new ContactDetailsController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator2(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = contactDetails(frontendAppConfig, form, NormalMode,
    firstIndex, establisherName)(fakeRequest, messages).toString

  val validData = Json.obj(
    SchemeDetailsId.toString -> SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherDetailsId.toString ->
          EstablisherDetails("test first name", None, "test last name", LocalDate.now),
        ContactDetailsId.toString ->
          ContactDetails("test@test.com", "123456789")
      ),
      Json.obj(
        EstablisherDetailsId.toString ->
          EstablisherDetails("test", None, "test", LocalDate.now)
      )
    )
  )

  "ContactDetails Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
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
      contentAsString(result) mustBe viewAsString(form.fill(ContactDetails("test@test.com", "123456789")))
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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "value 1"), ("phoneNumber", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
