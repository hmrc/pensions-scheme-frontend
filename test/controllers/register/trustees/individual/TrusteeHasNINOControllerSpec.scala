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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions.FakeDataRetrievalAction
import forms.HasReferenceNumberFormProvider
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNameId}
import models.person.PersonDetails
import models.{Index, NormalMode}
import navigators.Navigator
import org.joda.time.LocalDate
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.FakeNavigator
import utils.annotations.TrusteesIndividual
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.Future

class TrusteeHasNINOControllerSpec extends ControllerSpecBase {

  import TrusteeHasNINOControllerSpec._

  "TrusteeHasNINOController" must {
    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getMandatoryTrustee).build()

      val controller = app.injector.instanceOf[TrusteeHasNINOController]

      val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "return OK and the correct view for a GET where question already answered" in {
      val trusteeDataWithNinoAnswer = new FakeDataRetrievalAction(Some(validTrusteeData("hasNino" -> false)))

      val app = applicationBuilder(trusteeDataWithNinoAnswer).build()

      val controller = app.injector.instanceOf[TrusteeHasNINOController]

      val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString(form = form.fill(value = false))

      app.stop()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val app = applicationBuilder(getMandatoryTrustee)
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind(classOf[Navigator]).qualifiedWith(classOf[TrusteesIndividual])
            .toInstance(new FakeNavigator(onwardRoute))
        )
        .build()

      val validData = Json.obj(
        "trustees" -> Json.arr(
          Json.obj(
            TrusteeNameId.toString ->
              PersonDetails("Test", Some("Trustee"), "Name", LocalDate.now)
          )
        )
      )

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val controller = app.injector.instanceOf[TrusteeHasNINOController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller.onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)

      app.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val app = applicationBuilder(getMandatoryTrustee).build()

      val controller = app.injector.instanceOf[TrusteeHasNINOController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller.onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)

      app.stop()
    }
  }
}

object TrusteeHasNINOControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val schemeName = None

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("error", "test trustee name")
  private val index = Index(0)
  private val srn = None
  private val postCall = controllers.register.trustees.individual.routes.TrusteeHasNINOController.onSubmit(NormalMode, index, srn)
  private val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__genericHasNino__title", Message("messages__theTrustee").resolve),
    heading = Message("messages__genericHasNino__h1", "Test Name"),
    hint = None
  )

  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]

  private def viewAsString(form: Form[_] = form) = hasReferenceNumber(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString
}
