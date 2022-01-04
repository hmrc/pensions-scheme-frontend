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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.actions.FakeDataRetrievalAction
import controllers.register.trustees.partnership.routes.PartnershipHasVATController
import forms.HasReferenceNumberFormProvider
import identifiers.register.trustees.partnership.PartnershipHasVATId
import models.{Index, NormalMode}
import navigators.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.Future

class PartnershipHasVATControllerSpec extends ControllerSpecBase {

  import PartnershipHasVATControllerSpec._

  "PartnershipHasVatController" must {
    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getMandatoryTrusteePartnership).build()

      val controller = app.injector.instanceOf[PartnershipHasVATController]

      val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "return OK and the correct view for a GET where question already answered" in {
      val answered = new FakeDataRetrievalAction(Some(validTrusteePartnershipData("hasVat" -> false)))

      val app = applicationBuilder(answered).build()

      val controller = app.injector.instanceOf[PartnershipHasVATController]

      val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString(form.fill(value = false))

      app.stop()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val app = applicationBuilder(getMandatoryTrusteePartnership)
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute))
        )
        .build()

      val validData = UserAnswers().set(PartnershipHasVATId(index))(value = true).asOpt.value.json

      when(mockUserAnswersService.save(any(), any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(validData))

      val controller = app.injector.instanceOf[PartnershipHasVATController]

      val result = controller.onSubmit(NormalMode, index, None)(fakeRequest.withFormUrlEncodedBody(("value", "true")))

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)

      app.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val app = applicationBuilder(getMandatoryTrusteePartnership).build()

      val controller = app.injector.instanceOf[PartnershipHasVATController]

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller.onSubmit(NormalMode, index, None)(fakeRequest.withFormUrlEncodedBody(("value", "invalid value")))

      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)

      app.stop()
    }
  }
}

object PartnershipHasVATControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val schemeName = None
  private val partnershipName = "test partnership name"
  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("error", partnershipName)
  private val srn = None
  private val index = Index(0)

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val viewModel = CommonFormWithHintViewModel(
    postCall = PartnershipHasVATController.onSubmit(NormalMode, index, srn),
    title = Message("messages__hasVAT", Message("messages__thePartnership").resolve),
    heading = Message("messages__hasVAT", partnershipName),
    hint = None,
    srn = srn
  )

  private val mockUserAnswersService: UserAnswersService =
    mock[UserAnswersService]

  private val view = injector.instanceOf[hasReferenceNumber]

  private def viewAsString(form: Form[_] = form) =
    view(form, viewModel, schemeName)(fakeRequest, messages).toString
}
