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

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions._
import forms.UTRFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.partnership.{PartnershipDetailsId, PartnershipHasUTRId, PartnershipNoUTRReasonId, PartnershipEnterUTRId}
import models.{CheckUpdateMode, Index, NormalMode, PartnershipDetails, ReferenceValue}
import org.scalatest.MustMatchers
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class PartnershipEnterUTRControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipEnterUTRControllerSpec._

  "PartnershipEnterUTRController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted and clean up takes place" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", utrValue))

      val result = controller(getDataWithNoUtrReason).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.userAnswer.get(PartnershipEnterUTRId(index)).value mustBe ReferenceValue(utrValue, true)
      FakeUserAnswersService.userAnswer.get(PartnershipNoUTRReasonId(index)) mustBe None
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}



object PartnershipEnterUTRControllerSpec extends PartnershipEnterUTRControllerSpec {

  val formProvider = new UTRFormProvider()
  val form: Form[ReferenceValue] = formProvider()
  val index = Index(0)
  val srn = None
  val utrValue = "9999999999"

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = UTRViewModel(
    routes.PartnershipEnterUTRController.onSubmit(NormalMode, index, srn),
    title = Message("messages__enterUTR", Message("messages__thePartnership").resolve),
    heading = Message("messages__enterUTR", "test partnership name"),
    hint = Message("messages_utr__hint"),
    srn = srn
  )

  def viewAsString(form: Form[_] = form) =
    utr(frontendAppConfig, form, viewModel, schemeName = None)(fakeRequest, messages).toString

  def getDataWithNoUtrReason: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name"),
          PartnershipHasUTRId.toString -> false,
          PartnershipNoUTRReasonId.toString -> "utr number is not present"
        )
      )
    ))
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteePartnership): PartnershipEnterUTRController =
    new PartnershipEnterUTRController(
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
}









