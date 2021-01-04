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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.PhoneFormProvider
import identifiers.register.trustees.individual.TrusteeNameId
import models.person.PersonName
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phoneNumber

class TrusteePhoneControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PhoneFormProvider()
  val form: Form[String] = formProvider()
  val firstIndex = Index(0)
  val invalidValue = "invalid value"

  private val trusteeDataRetrievalAction = UserAnswers().set(TrusteeNameId(0))(PersonName("first", "last")).asOpt.value.dataRetrievalAction
  private val view = injector.instanceOf[phoneNumber]

  def controller(dataRetrievalAction: DataRetrievalAction = trusteeDataRetrievalAction): TrusteePhoneController =
    new TrusteePhoneController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeUserAnswersService,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      formProvider,
      stubMessagesControllerComponents(),
      view
    )

  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      CommonFormWithHintViewModel(
        routes.TrusteePhoneController.onSubmit(NormalMode, firstIndex, None),
        Message("messages__enterPhoneNumber", Message("messages__theIndividual").resolve),
        Message("messages__enterPhoneNumber", "first last"),
        Some(Message("messages__contact_details__hint", "first last")),
        None
      ),
      None
    )(fakeRequest, messages).toString

  "TrusteePhoneController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to relevant page" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("phone", "09090909090"))
        val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "yield BAD REQUEST when invalid value submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("phone", invalidValue))
        val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)
        val boundForm = form.bind(Map("phone" -> invalidValue))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }
  }
}