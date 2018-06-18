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
import forms.register.establishers.company.director.DirectorUniqueTaxReferenceFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorUniqueTaxReferenceId}
import models._
import models.register.establishers.company.director.DirectorDetails
import models.UniqueTaxReference
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, FakeNavigator2}
import views.html.register.establishers.company.director.directorUniqueTaxReference

class DirectorUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val invalidIndex = Index(10)
  val formProvider = new DirectorUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()
  val companyName = "test company name"
  val directorName = "First Name Middle Name Last Name"
  val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "director" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString ->
              DirectorDetails("First Name", Some("Middle Name"), "Last Name", LocalDate.now),
            DirectorUniqueTaxReferenceId.toString ->
              UniqueTaxReference.Yes("1234567891")
          )
        )
      )
    )
  )

  val validDataNoDirectorDetails = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "director" -> Json.arr(
          Json.obj(
            DirectorUniqueTaxReferenceId.toString ->
              UniqueTaxReference.Yes("1234567891")
          )
        )
      )
    )
  )

  val validDataEmptyForm = Json.obj(
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



  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): DirectorUniqueTaxReferenceController =
    new DirectorUniqueTaxReferenceController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator2(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = directorUniqueTaxReference(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex,directorName)(fakeRequest, messages).toString

  "DirectorUniqueTaxReference Controller" must {
    val getRelevantData = new FakeDataRetrievalAction(Some(validData))

    "return OK and the correct view for a GET when director name is present" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataEmptyForm))

      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to Session Expired page when the establisher index is not valid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, directorIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired page when the director index is not valid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataEmptyForm))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("uniqueTaxReference.hasUtr", "true"), ("uniqueTaxReference.utr", "1234565656"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
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

    "redirect to Session Expired for a GET if no director data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoDirectorDetails))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no director data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoDirectorDetails))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}

