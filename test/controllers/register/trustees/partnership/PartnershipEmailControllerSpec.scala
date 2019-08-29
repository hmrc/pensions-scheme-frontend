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
import controllers.actions._
import controllers.register.trustees.partnership.PartnershipHasVATControllerSpec.onwardRoute
import forms.EmailFormProvider
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
import views.html.emailAddress

class PartnershipEmailControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new EmailFormProvider()
  val form: Form[String] = formProvider()
  private val firstIndex = Index(0)
  private val trusteePartnershipDetails = PartnershipDetails("test partnership")
  private val email = "test@test.com"

  private val fullAnswers = UserAnswers().trusteePartnershipDetails(firstIndex, trusteePartnershipDetails)

  def viewAsString(form: Form[_] = form): String =
    emailAddress(
      frontendAppConfig,
      form,
      CommonFormWithHintViewModel(
        routes.PartnershipEmailController.onSubmit(NormalMode, firstIndex, None),
        Message("messages__partnership_email__title"),
        Message("messages__common_email__heading", trusteePartnershipDetails.name),
        Some(Message("messages__email__hint")),
        None
      ),
      None
    )(fakeRequest, messages).toString

  "PartnershipEmailController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true): _*)) {
          app =>
          val controller = app.injector.instanceOf[PartnershipEmailController]
          val result = controller.onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }

    "on a POST" must {
      "redirect to relevant page" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            ): _*)) {
          app =>
          val controller = app.injector.instanceOf[PartnershipEmailController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("email", email))
          val result = controller.onSubmit(NormalMode, firstIndex, None)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }
    }
  }
}

