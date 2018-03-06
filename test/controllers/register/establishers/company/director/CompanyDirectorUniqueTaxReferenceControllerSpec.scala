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
import forms.register.establishers.company.director.CompanyDirectorUniqueTaxReferenceFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.CompanyDirectorUniqueTaxReferenceId
import models._
import models.register.establishers.individual.UniqueTaxReference
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.company.director.companyDirectorUniqueTaxReference

class CompanyDirectorUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val invalidIndex = Index(10)
  val formProvider = new CompanyDirectorUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()

  val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "director" -> Json.arr(
          Json.obj(
            CompanyDirectorUniqueTaxReferenceId.toString ->
              UniqueTaxReference.Yes("1234567891")
          )
        )
      )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): CompanyDirectorUniqueTaxReferenceController =
    new CompanyDirectorUniqueTaxReferenceController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = companyDirectorUniqueTaxReference(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex)(fakeRequest, messages).toString

  "CompanyDirectorUniqueTaxReference Controller" must {

    "return OK and the correct view for a GET when director name is present" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to Session Expired page when the index is not valid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("uniqueTaxReference.hasUtr", "true"), ("uniqueTaxReference.utr", "1234565656"))
      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}

