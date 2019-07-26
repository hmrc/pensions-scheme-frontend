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
import controllers.actions._
import forms.HasBeenTradingFormProvider
import identifiers.register.trustees.company.HasBeenTradingCompanyId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class HasBeenTradingCompanyControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  val formProvider = new HasBeenTradingFormProvider()
  val form = formProvider("messages__tradingAtLeastOneYear__error","test company name")
  val index = Index(0)
  val srn = None
  val postCall = controllers.register.trustees.company.routes.HasBeenTradingCompanyController.onSubmit(NormalMode, index, srn)

  val viewModel = CommonFormWithHintViewModel(
    controllers.register.trustees.company.routes.HasBeenTradingCompanyController.onSubmit(NormalMode, index, srn),
    title = Message("messages__hasBeenTradingCompany__title"),
    heading = Message("messages__hasBeenTradingCompany__h1", "test company name"),
    hint = None
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany): HasBeenTradingCompanyController =
    new HasBeenTradingCompanyController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      global
    )

  private def viewAsString(form: Form[_] = form) = hasReferenceNumber(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyVatController" must {

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
      FakeUserAnswersService.userAnswer.get(HasBeenTradingCompanyId(index)).value mustEqual true
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

