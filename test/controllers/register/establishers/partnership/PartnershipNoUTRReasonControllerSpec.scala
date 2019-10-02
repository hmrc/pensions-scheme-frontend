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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import forms.ReasonFormProvider
import identifiers.register.establishers.partnership.PartnershipNoUTRReasonId
import models.{Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class PartnershipNoUTRReasonControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private val dummyNoUtrReason = "no utr"
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val partnershipDetails = PartnershipDetails("test partnership name")
  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", partnershipDetails.name)
  private val index = Index(0)
  private val srn = None
  private val fullAnswers = UserAnswers().establisherPartnershipDetails(index, partnershipDetails)

  private val viewModel = ReasonViewModel(
    postCall = routes.PartnershipNoUTRReasonController.onSubmit(NormalMode, index, srn),
    title = Message("messages__whyNoUTR", Message("messages__thePartnership").resolve),
    heading = Message("messages__whyNoUTR", partnershipDetails.name),
    srn = srn
  )

  private def viewAsString(form: Form[_] = form): String =
    reason(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipNoUTRReasonController" when {
    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipNoUTRReasonController]
            val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "return OK and the correct view where question already answered" in {
        running(_.overrides(modules(fullAnswers.set(PartnershipNoUTRReasonId(index))(dummyNoUtrReason).asOpt.value.dataRetrievalAction,
          featureSwitchEnabled = true): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipNoUTRReasonController]
            val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString(form.fill(value = dummyNoUtrReason))
        }
      }
    }

    "on a POST" must {
      "redirect to relevant page when valid data is submitted" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            ): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipNoUTRReasonController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", dummyNoUtrReason))
            val result = controller.onSubmit(NormalMode, index, None)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }

      "return a Bad Request and errors when invalid data is submitted" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            ): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipNoUTRReasonController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "{invalid value}"))
            val boundForm = form.bind(Map("reason" -> "{invalid value}"))
            val result = controller.onSubmit(NormalMode, index, None)(postRequest)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe viewAsString(boundForm)
        }
      }
    }
  }
}
