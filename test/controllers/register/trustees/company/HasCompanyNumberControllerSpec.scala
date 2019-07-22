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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import forms.HasCrnFormProvider
import identifiers.register.trustees.company.HasCompanyNumberId
import models.{Index, NormalMode}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, MockValidationHelper}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber


class HasCompanyNumberControllerSpec extends ControllerSpecBase with MockitoSugar with MockValidationHelper {
  private val schemeName = None

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new HasCrnFormProvider()
  private val form = formProvider("messages__hasCompanyNumber__error__required", "test company name")
  private val index = Index(0)
  private val srn = None
  private val postCall = controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit(NormalMode, srn, index)

  private val viewModel = CommonFormWithHintViewModel(
    controllers.register.trustees.company.routes.HasCompanyNumberController.onSubmit(NormalMode, index, srn),
    title = Message("messages__hasCompanyCompanyNumber__title"),
    heading = Message("messages__hasCompanyNumber__h1", "test company name"),
    hint = Some(Message("messages__hasCompanyNumber__p1"))
  )

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): HasCompanyNumberController =
    new HasCompanyNumberController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form) = hasReferenceNumber(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyNumberController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.userAnswer.get(HasCompanyNumberId(index)).value mustEqual true

    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}

