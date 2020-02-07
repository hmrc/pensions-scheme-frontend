/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import forms.ReasonFormProvider
import identifiers.register.trustees.individual.TrusteeNoNINOReasonId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class TrusteeNoNINOReasonControllerSpec extends ControllerSpecBase {
    private val schemeName = None
    private def onwardRoute = controllers.routes.IndexController.onPageLoad()
    val formProvider = new ReasonFormProvider()
    val name = "Test Name"
    val form = formProvider("messages__reason__error_ninoRequired", name)
    val index = Index(0)
    val srn = None
    val postCall = routes.TrusteeNoNINOReasonController.onSubmit(NormalMode, index, srn)

  val viewmodel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__whyNoNINO", Message("messages__theIndividual").resolve),
    heading = Message("messages__whyNoNINO", name),
    srn = srn
  )
  private val view = injector.instanceOf[reason]

    def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrustee): TrusteeNoNINOReasonController =
      new TrusteeNoNINOReasonController(
        frontendAppConfig,
        messagesApi,
        FakeUserAnswersService,
        new FakeNavigator(desiredRoute = onwardRoute),
        FakeAuthAction,
        dataRetrievalAction,
        FakeAllowAccessProvider(),
        new DataRequiredActionImpl,
        formProvider,
        stubMessagesControllerComponents(),
        view
      )

    private def viewAsString(form: Form[_] = form) = view(form, viewmodel, schemeName)(fakeRequest, messages).toString

    "TrusteeNoNinoReasonController" must {

      "return OK and the correct view for a GET" in {
        val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "redirect to the next page when valid data is submitted for true" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "reason"))

        val result = controller().onSubmit(NormalMode, index, None)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        FakeUserAnswersService.verify(TrusteeNoNINOReasonId(index), "reason")
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

