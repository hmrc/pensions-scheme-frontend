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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import forms.EmailFormProvider
import identifiers.register.establishers.company.director.DirectorNameId
import models.person.PersonName
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, UserAnswers, _}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.emailAddress

class DirectorEmailControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new EmailFormProvider()
  private val form: Form[String] = formProvider()
  private val firstIndex = Index(0)

  private val estCompanyDirector = UserAnswers().set(DirectorNameId(0, 0))(PersonName("first", "last")).asOpt.value.dataRetrievalAction

  private val view = injector.instanceOf[emailAddress]

  def controller(dataRetrievalAction: DataRetrievalAction = estCompanyDirector): DirectorEmailController =
    new DirectorEmailController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeUserAnswersService,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      formProvider,
      view,
      controllerComponents
    )

  def viewAsString(form: Form[?] = form): String =
    view(
      form,
      CommonFormWithHintViewModel(
        routes.DirectorEmailController.onSubmit(NormalMode, firstIndex, firstIndex, EmptyOptionalSchemeReferenceNumber),
        Message("messages__director_email__title"),
        Message("messages__enterEmail", "first last"),
        Some(Message("messages__contact_details__hint", "first last")),
        EmptyOptionalSchemeReferenceNumber
      ),
      None
    )(fakeRequest, messages).toString

  "DirectorEmailController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, firstIndex, firstIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to relevant page" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("email", "test@test.com"))
        val result = controller().onSubmit(NormalMode, firstIndex, firstIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

