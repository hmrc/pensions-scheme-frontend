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
import forms.ContactDetailsFormProvider
import identifiers.register.trustees.individual.{TrusteeContactDetailsId, TrusteeDetailsId}
import models.person.PersonDetails
import models.{ContactDetails, Index, Mode, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.{AnyContent, Call, Request}
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{ContactDetailsViewModel, Message}
import views.html.contactDetails

class TrusteeContactDetailsControllerSpec extends ControllerSpecBase {

  import TrusteeContactDetailsControllerSpec._

  "TrusteeContactDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(trusteeData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val filledForm = form.fill(contactDetailsModel)
      val result = controller(trusteeAndAnswerData(contactDetailsModel)).onPageLoad(NormalMode, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(filledForm)
    }

    "redirect to the next page when valid data is submitted" in {
      val request = postRequest(contactDetailsModel)

      val result = controller(trusteeData).onSubmit(NormalMode, index)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the answer" in {
      val request = postRequest(contactDetailsModel)

      val result = controller(trusteeData).onSubmit(NormalMode, index)(request)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(TrusteeContactDetailsId(index), contactDetailsModel)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val errorForm = form.bind(Map.empty[String, String])
      assume(errorForm.hasErrors)

      val result = controller(trusteeData).onSubmit(NormalMode, index)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(errorForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}

object TrusteeContactDetailsControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val index = Index(0)

  private val trustee = PersonDetails(
    "John",
    None,
    "Doe",
    LocalDate.now()
  )

  private val contactDetailsModel = ContactDetails(
    "john.doe@doe.net",
    "1234567890"
  )

  private val formProvider = new ContactDetailsFormProvider()
  private val form = formProvider()

  private def viewmodel(mode: Mode, index: Index, trusteeName: String) = ContactDetailsViewModel(
    postCall = routes.TrusteeContactDetailsController.onSubmit(mode, index, None),
    title = Message("messages__trustee_contact_details__title"),
    heading = Message("messages__trustee_contact_details__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(trusteeName)
  )

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new TrusteeContactDetailsController(
      new FakeNavigator(desiredRoute = onwardRoute),
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form) =
    contactDetails(
      frontendAppConfig,
      form,
      viewmodel(NormalMode, index, trustee.fullName),
      None
    )(fakeRequest, messages).toString

  private def trusteeUserAnswers: UserAnswers = {
    UserAnswers().set(TrusteeDetailsId(index))(trustee) match {
      case JsSuccess(userAnswers, _) => userAnswers
      case JsError(errors) => throw JsResultException(errors)
    }
  }

  private def trusteeData: DataRetrievalAction = {
    new FakeDataRetrievalAction(Some(trusteeUserAnswers.json))
  }

  private def trusteeAndAnswerData(answer: ContactDetails): DataRetrievalAction = {
    trusteeUserAnswers.set(TrusteeContactDetailsId(index))(answer) match {
      case JsSuccess(userAnswers, _) => new FakeDataRetrievalAction(Some(userAnswers.json))
      case JsError(errors) => throw JsResultException(errors)
    }
  }

  private def postRequest(contactDetails: ContactDetails): Request[AnyContent] = {
    fakeRequest.withFormUrlEncodedBody(
      ("emailAddress", contactDetails.emailAddress),
      ("phoneNumber", contactDetails.phoneNumber)
    )
  }

}
