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
import forms.HasUTRFormProvider
import identifiers.register.establishers.partnership.PartnershipHasUTRId
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, OptionalSchemeReferenceNumber, PartnershipDetails}
import navigators.Navigator
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class PartnershipHasUTRControllerSpec extends ControllerSpecBase {
  private val schemeName = None

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val partnershipDetails = PartnershipDetails("test partnership name")
  private val formProvider = new HasUTRFormProvider()
  private val form = formProvider("messages__hasUtr__partnership_error_required", partnershipDetails.name)
  private val index = Index(0)
  private val srn = None
  private val fullAnswers = UserAnswers().establisherPartnershipDetails(Index(0), partnershipDetails)

  private val viewModel = CommonFormWithHintViewModel(
    postCall = routes.PartnershipHasUTRController.onSubmit(NormalMode, Index(0), OptionalSchemeReferenceNumber(srn)),
    title = Message("messages__hasUTR", Message("messages__thePartnership").resolve),
    heading = Message("messages__hasUTR", partnershipDetails.name),
    hint = Some(Message("messages__hasUtr__p1")),
    srn = OptionalSchemeReferenceNumber(srn)
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  private def viewAsString(form: Form[_] = form): String =
    view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipHasUTRController" when {
    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipHasUTRController]
            val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "return OK and the correct view where question already answered" in {
        running(_.overrides(modules(fullAnswers.set(PartnershipHasUTRId(index))(value = false).asOpt.value.dataRetrievalAction): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipHasUTRController]
            val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString(form.fill(value = false))
        }
      }
    }

    "on a POST" must {
      "redirect to relevant page when valid data is submitted" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            ): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipHasUTRController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
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
            ): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipHasUTRController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
            val boundForm = form.bind(Map("value" -> "invalid value"))
            val result = controller.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe viewAsString(boundForm)
        }
      }
    }
  }
}
