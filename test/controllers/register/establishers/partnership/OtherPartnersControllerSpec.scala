/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.partnership.OtherPartnersFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.{OtherPartnersId, PartnershipDetailsId}
import models.{Index, NormalMode, PartnershipDetails}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import services.FakeUserAnswersService

import utils.FakeNavigator
import views.html.register.establishers.partnership.otherPartners

class OtherPartnersControllerSpec extends ControllerSpecBase {

  //scalastyle:off magic.number

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val schemeName = "Test Scheme Name"

  val index = Index(0)
  val invalidIndex = Index(10)

  val formProvider = new OtherPartnersFormProvider()
  val form = formProvider()
  val partnershipName = "test partnership name"

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails("test partnership name", false),
        OtherPartnersId.toString -> true
      )
    )
  )
  private val view = injector.instanceOf[otherPartners]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership): OtherPartnersController =
    new OtherPartnersController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )
  val submitUrl = controllers.register.establishers.partnership.routes.OtherPartnersController.onSubmit(NormalMode, index, None)
  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      NormalMode,
      index,
      None,
      submitUrl,
      None
    )(fakeRequest, messages).toString

  "OtherPartners Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {


      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index, None)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to session expired page on a GET when the index is not valid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
