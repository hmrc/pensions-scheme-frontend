/*
 * Copyright 2024 HM Revenue & Customs
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
import identifiers.register.establishers.partnership.PartnershipEnterUTRId
import models.*
import navigators.Navigator
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, *}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswerOps, UserAnswers}
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class PartnershipEnterUTRControllerSpec extends ControllerSpecBase {
  appRunning()
  private val schemeName = None
  private val dummyUtr = "1111111111"
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val partnershipDetails = PartnershipDetails("test partnership name")
  private val formProvider = new UTRFormProvider()
  private val form = formProvider()
  private val index = Index(0)
  private val srn = None
  private val fullAnswers = UserAnswers().establisherPartnershipDetails(Index(0), partnershipDetails)

  private val viewModel = UTRViewModel(
    postCall = routes.PartnershipEnterUTRController.onSubmit(NormalMode, Index(0), OptionalSchemeReferenceNumber(srn)),
    title = Message("messages__enterUTR", Message("messages__thePartnership").resolve),
    heading = Message("messages__enterUTR", partnershipDetails.name),
    hint = Message("messages_utr__hint"),
    srn = OptionalSchemeReferenceNumber(srn)
  )

  private val view = injector.instanceOf[utr]

  private def viewAsString(form: Form[?] = form): String =
    view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipHasUTRController" when {
    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction)*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipEnterUTRController]
            val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "return OK and the correct view where question already answered" in {
        val testUtr = ReferenceValue(dummyUtr)
        running(_.overrides(modules(fullAnswers.set(PartnershipEnterUTRId(index))(testUtr).asOpt.value.dataRetrievalAction)*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipEnterUTRController]
            val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString(form.fill(value = testUtr))
        }
      }
    }

    "on a POST" must {
      "redirect to relevant page when valid data is submitted" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            )*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipEnterUTRController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", dummyUtr))
            val result = controller.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }

      "return a Bad Request and errors when invalid data is submitted" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            )*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipEnterUTRController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", "invalid value"))
            val boundForm = form.bind(Map("utr" -> "invalid value"))
            val result = controller.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe viewAsString(boundForm)
        }
      }
    }
  }
}
