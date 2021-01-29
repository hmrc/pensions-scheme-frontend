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
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import forms.ReasonFormProvider
import identifiers.register.establishers.partnership.partner.PartnerNoNINOReasonId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService

import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class PartnerNoNINOReasonControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val formProvider = new ReasonFormProvider()
  private val name = "first last"
  private val form = formProvider("messages__reason__error_ninoRequired", name)
  private val establisherIndex, partnerIndex = Index(0)
  private val srn = None
  private val postCall = routes.PartnerNoNINOReasonController.onSubmit(NormalMode, establisherIndex, partnerIndex, srn)

  private val viewModel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__whyNoNINO", Message("messages__thePartner")),
    heading = Message("messages__whyNoNINO", name),
    srn = srn
  )
  private val view = injector.instanceOf[reason]
  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryPartner): PartnerNoNINOReasonController =
    new PartnerNoNINOReasonController(
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

  private def viewAsString(form: Form[_] = form): String = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyCRNController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "reason"))

      val result = controller().onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(PartnerNoNINOReasonId(establisherIndex, partnerIndex), "reason")
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}

