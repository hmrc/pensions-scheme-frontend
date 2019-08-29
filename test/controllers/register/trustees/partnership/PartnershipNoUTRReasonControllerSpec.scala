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

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import forms.ReasonFormProvider
import identifiers.TypedIdentifier
import identifiers.register.trustees.partnership.PartnershipNoUTRReasonId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class PartnershipNoUTRReasonControllerSpec extends ControllerSpecBase {

  import PartnershipNoUTRReasonControllerSpec._

  "PartnershipNoUTRControllerSpec" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "valid reason"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(PartnershipNoUTRReasonId(index), "valid reason")
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


object PartnershipNoUTRReasonControllerSpec extends ControllerSpecBase {

  object FakeIdentifier extends TypedIdentifier[String]

  val partnershipName = "test partnership name"
  val index = Index(0)
  val errorKey = "messages__reason__error_utrRequired"

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  val formProvider = new ReasonFormProvider()
  val form = formProvider("messages__reason__error_utrRequired", partnershipName)

  val viewmodel = ReasonViewModel(
    postCall = routes.PartnershipNoUTRReasonController.onSubmit(NormalMode, index, None),
    title = Message("messages__partnershipNoUtr__title"),
    heading = Message("messages__noGenericUtr__heading", partnershipName)
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteePartnership): PartnershipNoUTRReasonController =
    new PartnershipNoUTRReasonController(
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

  private def viewAsString(form: Form[_] = form) = reason(frontendAppConfig, form, viewmodel, None)(fakeRequest, messages).toString

}



