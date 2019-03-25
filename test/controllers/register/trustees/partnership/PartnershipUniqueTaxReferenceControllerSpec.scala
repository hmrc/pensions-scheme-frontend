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

package controllers.register.trustees.partnership

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.register.trustees.partnership.PartnershipUniqueTaxReferenceFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipUniqueTaxReferenceId}
import models.{Index, NormalMode, PartnershipDetails, UniqueTaxReference}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.trustees.partnership.partnershipUniqueTaxReference

class PartnershipUniqueTaxReferenceControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val firstIndex = Index(0)
  val formProvider = new PartnershipUniqueTaxReferenceFormProvider()
  val form: Form[UniqueTaxReference] = formProvider()
  val partnershipName = "test partnership name"

  val validData: JsObject = Json.obj(
    TrusteesId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString ->
          PartnershipDetails("test partnership name"),
        PartnershipUniqueTaxReferenceId.toString ->
          UniqueTaxReference.Yes("1234567891")
      ),
      Json.obj(
        PartnershipDetailsId.toString ->
          PartnershipDetails("test")
      )
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteePartnership): PartnershipUniqueTaxReferenceController =
    new PartnershipUniqueTaxReferenceController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      new FakeNavigator(desiredRoute = onwardRoute),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )
  val submitUrl = controllers.register.trustees.partnership.routes.PartnershipUniqueTaxReferenceController.onSubmit(NormalMode, firstIndex, None)
  def viewAsString(form: Form[_] = form): String =
    partnershipUniqueTaxReference(
      frontendAppConfig,
      form,
      NormalMode,
      firstIndex,
      None,
      submitUrl
    )(fakeRequest, messages).toString

  "PartnershipUniqueTaxReference Controller" must {

    "return OK and the correct view for a GET when partnership name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
