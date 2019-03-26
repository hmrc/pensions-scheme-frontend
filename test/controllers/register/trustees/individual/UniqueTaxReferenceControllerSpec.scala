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

package controllers.register.trustees.individual

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.individual.UniqueTaxReferenceFormProvider
import identifiers.register.trustees.individual.{TrusteeDetailsId, UniqueTaxReferenceId}
import models.person.PersonDetails
import models.{Index, NormalMode, UniqueTaxReference}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.trustees.individual.uniqueTaxReference

class UniqueTaxReferenceControllerSpec extends ControllerSpecBase {
  appRunning()
  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new UniqueTaxReferenceFormProvider()
  val form = formProvider()

  val index = Index(0)
  val trustee = "Test Trustee Name"

  val validData = Json.obj(
    "trustees" -> Json.arr(
      Json.obj(
        TrusteeDetailsId.toString ->
          PersonDetails("Test", Some("Trustee"), "Name", LocalDate.now),
        UniqueTaxReferenceId.toString ->
          UniqueTaxReference.Yes("1234567891")
      )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrustee): UniqueTaxReferenceController =
    new UniqueTaxReferenceController(frontendAppConfig, messagesApi, FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  private val submitUrl = controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onSubmit(NormalMode, index, None)

  private def viewAsString(form: Form[_] = form) =
    uniqueTaxReference(frontendAppConfig, form, NormalMode, index, None, submitUrl)(fakeRequest, messages).toString

  "UniqueTaxReference Controller" must {

    "return OK and the correct view for a GET when trustee name is present" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to Session Expired page when the index is not valid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, Index(2))(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("uniqueTaxReference.hasUtr", "true"), ("uniqueTaxReference.utr", "1234565656"))
      val result = controller().onSubmit(NormalMode, index)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, index)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
