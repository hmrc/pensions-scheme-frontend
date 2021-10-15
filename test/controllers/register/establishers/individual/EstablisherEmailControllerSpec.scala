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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.register.establishers.individual.routes.EstablisherEmailController
import forms.EmailFormProvider
import models.person.PersonName
import models.{Index, NormalMode}
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

class EstablisherEmailControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new EmailFormProvider()
  private val form: Form[String] = formProvider()
  private val firstIndex = Index(0)
  private val establisherName = PersonName("test", "name")
  private val email = "test@test.com"

  private val fullAnswers = UserAnswers().establishersIndividualName(firstIndex, establisherName)

  private val view = injector.instanceOf[emailAddress]

  private def viewAsString(form: Form[_] = form): String =
    view(
      form,
      CommonFormWithHintViewModel(
        EstablisherEmailController.onSubmit(NormalMode, firstIndex, None),
        Message("messages__enterEmail", Message("messages__theIndividual").resolve),
        Message("messages__enterEmail", establisherName.fullName),
        Some(Message("messages__contact_details__hint", establisherName.fullName)),
        None
      ),
      None
    )(fakeRequest, messages).toString

  "EstablisherEmailController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction): _*)) {
          app =>
            val controller = app.injector.instanceOf[EstablisherEmailController]
            val result = controller.onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }
    }

    "on a POST" must {
      "redirect to relevant page" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            ): _*)) {
          app =>
            val controller = app.injector.instanceOf[EstablisherEmailController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("email", email))
            val result = controller.onSubmit(NormalMode, firstIndex, None)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }
    }
  }
}

