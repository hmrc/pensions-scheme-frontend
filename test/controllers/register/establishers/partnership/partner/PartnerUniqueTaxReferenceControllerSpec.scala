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

import services.FakeUserAnswersService
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.partnership.partner.PartnerUniqueTaxReferenceFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.{PartnerDetailsId, PartnerUniqueTaxReferenceId}
import models.person.PersonDetails
import models.{UniqueTaxReference, _}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.partnership.partner.partnerUniqueTaxReference

//scalastyle:off magic.number

class PartnerUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val establisherIndex = Index(0)
  val partnerIndex = Index(0)
  val invalidIndex = Index(10)
  val formProvider = new PartnerUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()
  val partnershipName = "test partnership name"
  val partnerName = "First Name Middle Name Last Name"

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "partner" -> Json.arr(
          Json.obj(
            PartnerDetailsId.toString ->
              PersonDetails("First Name", Some("Middle Name"), "Last Name", LocalDate.now),
            PartnerUniqueTaxReferenceId.toString ->
              UniqueTaxReference.Yes("1234567891")
          )
        )
      )
    )
  )

  val validDataNoPartnerDetails: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "partner" -> Json.arr(
          Json.obj(
            PartnerUniqueTaxReferenceId.toString ->
              UniqueTaxReference.Yes("1234567891")
          )
        )
      )
    )
  )

  val validDataEmptyForm: JsObject = Json.obj(
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

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership): PartnerUniqueTaxReferenceController =
    new PartnerUniqueTaxReferenceController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider
    )
  val submitUrl = controllers.register.establishers.partnership.partner.routes.
    PartnerUniqueTaxReferenceController.onSubmit(NormalMode, establisherIndex, partnerIndex, None)
  def viewAsString(form: Form[_] = form): String =
    partnerUniqueTaxReference(
      frontendAppConfig,
      form,
      NormalMode,
      establisherIndex,
      partnerIndex,
      None,
      submitUrl
    )(fakeRequest, messages).toString

  "PartnerUniqueTaxReference Controller" must {
    val getRelevantData = new FakeDataRetrievalAction(Some(validData))

    "return OK and the correct view for a GET when partner name is present" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataEmptyForm))

      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(UniqueTaxReference.Yes("1234567891")))
    }

    "redirect to Session Expired page when the establisher index is not valid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, partnerIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired page when the partner index is not valid" ignore {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataEmptyForm))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("uniqueTaxReference.hasUtr", "true"), ("uniqueTaxReference.utr", "1234565656"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no partner data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPartnerDetails))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no partner data is found" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPartnerDetails))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}

