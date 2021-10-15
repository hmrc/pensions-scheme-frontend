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

package controllers.register.establishers.partnership.partner

import controllers.ControllerSpecBase
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.establishers.partnership.partner.PartnerNoUTRReasonId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class PartnerNoUTRReasonControllerSpec extends ControllerSpecBase {

  import PartnerNoUTRReasonControllerSpec._

  "PartnerNoUTRReasonController" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK and the correct view for a GET where valid reason given" in {
      val validData = validPartnerData("noUtrReason" -> "new reason")

      val dataRetrievalAction = new FakeDataRetrievalAction(Some(validData))
      val result = controller(dataRetrievalAction = dataRetrievalAction).onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form = form.fill("new reason"))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "new reason"))

      val result = controller().onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(PartnerNoUTRReasonId(establisherIndex, partnerIndex), "new reason")
    }

    "return a Bad Request when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", ""))

      val result = controller().onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
    }
  }
}

object PartnerNoUTRReasonControllerSpec extends ControllerSpecBase {
  private val schemeName = None

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", "test partner name")
  private val establisherIndex = Index(0)
  private val partnerIndex = Index(0)
  private val srn = None
  private val postCall =
    controllers.register.establishers.partnership.partner.routes.PartnerNoUTRReasonController.onSubmit(NormalMode, establisherIndex, partnerIndex, srn)
  private val viewModel = ReasonViewModel(
    postCall,
    title = Message("messages__whyNoUTR", Message("messages__thePartner")),
    heading = Message("messages__whyNoUTR", "first last"),
    srn = srn
  )
  private val view = injector.instanceOf[reason]
  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryPartner): PartnerNoUTRReasonController =
    new PartnerNoUTRReasonController(
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

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString
}

