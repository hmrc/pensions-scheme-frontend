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

package controllers.register.establishers.company.director

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorContactDetailsId, DirectorDetailsId}
import models.{ContactDetails, _}
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.company.director.directorContactDetails

//scalastyle:off magic.number

class DirectorContactDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val companyName = "test company name"
  val formProvider = new ContactDetailsFormProvider()
  val form = formProvider()
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val invalidIndex = Index(10)
  val directorName = "First Name Middle Name Last Name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): DirectorContactDetailsController =
    new DirectorContactDetailsController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = directorContactDetails(frontendAppConfig, form, NormalMode,
    establisherIndex, directorIndex, directorName)(fakeRequest, messages).toString

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName, Some("123456"), Some("abcd")),
        "director" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString ->
              DirectorDetails("First Name", Some("Middle Name"), "Last Name", LocalDate.now),
            DirectorContactDetailsId.toString ->
              ContactDetails("test@test.com", "123456789"))
        )
      )
    )
  )

  val validDataNoPreviousAnswer: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName, Some("123456"), Some("abcd")),
        "director" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString ->
              DirectorDetails("First Name", Some("Middle Name"), "Last Name", LocalDate.now)
          )
        )
      )
    )
  )

  val validDataNoDirectorDetails: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName, Some("123456"), Some("abcd")),
        "director" -> Json.arr()
        )
      )
    )

  "DirectorContactDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired from a GET when the establisher index is invalid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a GET when the director index is invalid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(ContactDetails("test@test.com", "123456789")))
    }

    "redirect to the next page when valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no existing Director Details data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoDirectorDetails))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired for a POST if no existing director details data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoDirectorDetails))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))

      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a POST when the establisher index is invalid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onSubmit(NormalMode, invalidIndex, establisherIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a POST when the director index is invalid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
