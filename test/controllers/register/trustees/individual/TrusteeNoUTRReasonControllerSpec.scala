/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import forms.ReasonFormProvider
import identifiers.register.trustees.individual.TrusteeNameId
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.Future

class TrusteeNoUTRReasonControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", "Test Name")
  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val postCall = controllers.register.trustees.individual.routes.TrusteeNoUTRReasonController.onSubmit(NormalMode, Index(0), None)
  private val viewModel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__whyNoUTR", Message("messages__theIndividual").resolve),
    heading = Message("messages__whyNoUTR", "Test Name"),
    srn = None
  )

  private val view = injector.instanceOf[reason]

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, None)(fakeRequest, messages).toString

  "TrusteeNoUTRReasonController" must {
    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getMandatoryTrustee).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val result = controller.onPageLoad(NormalMode, Index(0), None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "return OK and the correct view for a GET where valid reason given" in {
      val trusteeDataWithNoUTRReasonAnswer = new FakeDataRetrievalAction(Some(validTrusteeData("noUtrReason" -> "blah")))

      val app = applicationBuilder(trusteeDataWithNoUTRReasonAnswer).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val result = controller.onPageLoad(NormalMode, Index(0), None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString(form = form.fill(value = "blah"))

      app.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      val app = applicationBuilder(getMandatoryTrustee)
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute))
        ).build()

      val validData = Json.obj(
        "trustees" -> Json.arr(
          Json.obj(
            TrusteeNameId.toString ->
              PersonName("Test", "Name")
          )
        )
      )

      when(mockUserAnswersService.save(any(), any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(validData))

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "blah"))

      val result = controller.onSubmit(NormalMode, Index(0), None)(postRequest)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)

      app.stop()
    }

    "return a Bad Request when invalid data is submitted" in {
      val app = applicationBuilder(getMandatoryTrustee).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("noUtrReason", ""))

      val boundForm = form.bind(Map("noUtrReason" -> ""))

      val result = controller.onSubmit(NormalMode, Index(0), None)(postRequest)

      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)

      app.stop()
    }
  }
}
