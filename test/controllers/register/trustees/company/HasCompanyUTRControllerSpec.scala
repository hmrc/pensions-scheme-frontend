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
import forms.HasUtrFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company._
import models.{CompanyDetails, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasUtr

class HasCompanyUTRControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val formProvider = new HasUtrFormProvider()
  private val form = formProvider("messages__hasCompanyUtr__error__required","test company name")
  private val index = Index(0)
  private val srn = None
  private val postCall = controllers.register.trustees.company.routes.HasCompanyUTRController.onSubmit(NormalMode, index, srn)
  private val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__hasCompanyUtr__title"),
    heading = Message("messages__hasCompanyUtr__h1", "test company name"),
    hint = Some(Message("messages__hasUtr__p1"))
  )

  private def getTrusteeCompanyPlusUtr(hasUtrValue:Boolean): FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name"),
          HasCompanyUTRId.toString -> hasUtrValue,
          NoUTRId.toString -> "utr number is not present",
          CompanyUTRId.toString -> "9999999999"
        )
      )
    ))
  )

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany): HasCompanyUTRController =
    new HasCompanyUTRController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form) = hasUtr(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyUTRController" must {

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
      FakeUserAnswersService.userAnswer.get(HasCompanyUTRId(index)).value mustEqual true
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "if user changes answer from yes to no then clean up should take place on utr number" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = controller(getTrusteeCompanyPlusUtr(hasUtrValue = true)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.userAnswer.get(HasCompanyUTRId(index)).value mustEqual false
      FakeUserAnswersService.userAnswer.get(CompanyUTRId(index)) mustBe None
    }

    "if user changes answer from no to yes then clean up should take place on no utr reason" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getTrusteeCompanyPlusUtr(hasUtrValue = false)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.userAnswer.get(HasCompanyUTRId(index)).value mustEqual true
      FakeUserAnswersService.userAnswer.get(NoUTRId(index)) mustBe None
    }

  }
}
