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
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.partnership.PartnershipHasVATId
import models.{Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class PartnershipHasVATControllerSpec extends ControllerSpecBase {

  private val schemeName = None
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val partnershipDetails = PartnershipDetails("test partnership name")
  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("messages__vat__formError", partnershipDetails.name)
  private val index = Index(0)
  private val srn = None
  private val postCall = controllers.register.establishers.partnership.routes.PartnershipHasVATController.onSubmit(NormalMode, index, srn)
  private val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__hasVAT", Message("messages__thePartnership").resolve),
    heading = Message("messages__hasVAT", partnershipDetails.name),
    hint = None,
    srn = srn
  )
  private val fullAnswers = UserAnswers().establisherPartnershipDetails(index, partnershipDetails)
  private def viewAsString(form: Form[_] = form): String =
    hasReferenceNumber(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipHasVATController" when {
    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipHasVATController]
            val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }

      "return OK and the correct view where question already answered" in {
        running(_.overrides(modules(fullAnswers.set(PartnershipHasVATId(index))(value = false).asOpt.value.dataRetrievalAction,
          featureSwitchEnabled = true): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipHasVATController]
            val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString(form.fill(value = false))
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
            val controller = app.injector.instanceOf[PartnershipHasVATController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
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
            val controller = app.injector.instanceOf[PartnershipHasVATController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
            val boundForm = form.bind(Map("value" -> "invalid value"))
            val result = controller.onSubmit(NormalMode, index, None)(postRequest)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe viewAsString(boundForm)
        }
      }
    }
  }
}
