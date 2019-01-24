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

package controllers.register.establishers.partnership.partner

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{PartnerContactDetailsId, PartnerDetailsId}
import models.person.PersonDetails
import models.{ContactDetails, _}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.{ContactDetailsViewModel, Message}
import views.html.contactDetails

//scalastyle:off magic.number

class PartnerContactDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val partnershipName = "test partnership name"
  val formProvider = new ContactDetailsFormProvider()
  val form = formProvider()
  val establisherIndex = Index(0)
  val partnerIndex = Index(0)
  val invalidIndex = Index(10)
  val partnerName = "First Name Middle Name Last Name"

  private def viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index, partnerName: String) = ContactDetailsViewModel(
    postCall = routes.PartnerContactDetailsController.onSubmit(mode, establisherIndex, partnerIndex),
    title = Message("messages__partner_contact__title"),
    heading = Message("messages__partner_contact__heading"),
    body = Message("messages__partner_contact__body"),
    subHeading = Some(partnerName)
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership): PartnerContactDetailsController =
    new PartnerContactDetailsController(
      new FakeNavigator(desiredRoute = onwardRoute),
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String = contactDetails(
    frontendAppConfig,
    form,
    viewmodel(NormalMode, establisherIndex, partnerIndex, partnerName),
    None
  )(fakeRequest, messages).toString

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
        "partner" -> Json.arr(
          Json.obj(
            PartnerDetailsId.toString ->
              PersonDetails("First Name", Some("Middle Name"), "Last Name", LocalDate.now),
            PartnerContactDetailsId.toString ->
              ContactDetails("test@test.com", "123456789"))
        )
      )
    )
  )

  val validDataNoPreviousAnswer: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
        "partner" -> Json.arr(
          Json.obj(
            PartnerDetailsId.toString ->
              PersonDetails("First Name", Some("Middle Name"), "Last Name", LocalDate.now)
          )
        )
      )
    )
  )

  val validDataNoPartnerDetails: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails(partnershipName),
        "partner" -> Json.arr()
      )
    )
  )

  "PartnerContactDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired from a GET when the establisher index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a GET when the partner index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, partnerIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(ContactDetails("test@test.com", "123456789")))
    }

    "redirect to the next page when valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, partnerIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, partnerIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no existing Partner Details data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPartnerDetails))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, partnerIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, partnerIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired for a POST if no existing partner details data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPartnerDetails))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("emailAddress", "test@test.com"), ("phoneNumber", "123456789"))

      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, partnerIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
