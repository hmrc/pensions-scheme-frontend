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
import forms.UTRFormProvider
import identifiers.register.establishers.partnership.PartnershipUTRId
import models.{Index, NormalMode, PartnershipDetails, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class PartnershipEnterUTRControllerSpec extends ControllerSpecBase {
  appRunning()
  private val schemeName = None
  private val dummyUtr = "1111111111"
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val partnershipDetails = PartnershipDetails("test partnership name")
  private val formProvider = new UTRFormProvider()
  private val form = formProvider()
  private val index = Index(0)
  private val srn = None
  private val fullAnswers = UserAnswers().establisherPartnershipDetails(index, partnershipDetails)

  private val viewModel = UTRViewModel(
    postCall = routes.PartnershipUTRController.onSubmit(NormalMode, index, srn),
    title = Message("messages__common_partnershipUtr__title"),
    heading = Message("messages__enterUTR", partnershipDetails.name),
    hint = Message("messages_utr__hint"),
    srn = srn
  )

  private def viewAsString(form: Form[_] = form): String =
    utr(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipHasUTRController" when {
    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipUTRController]
            val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "return OK and the correct view where question already answered" in {
        val testUtr = ReferenceValue(dummyUtr)
        running(_.overrides(modules(fullAnswers.set(PartnershipUTRId(index))(testUtr).asOpt.value.dataRetrievalAction,
          featureSwitchEnabled = true): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipUTRController]
            val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString(form.fill(value = testUtr))
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
            val controller = app.injector.instanceOf[PartnershipUTRController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", dummyUtr))
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
            val controller = app.injector.instanceOf[PartnershipUTRController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", "invalid value"))
            val boundForm = form.bind(Map("utr" -> "invalid value"))
            val result = controller.onSubmit(NormalMode, index, None)(postRequest)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe viewAsString(boundForm)
        }
      }
    }
  }
}
