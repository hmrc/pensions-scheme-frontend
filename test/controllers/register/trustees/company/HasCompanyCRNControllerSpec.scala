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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasCRNFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.{CompanyEnterCRNId, CompanyNoCRNReasonId, HasCompanyCRNId}
import models.{CompanyDetails, Index, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, MockValidationHelper}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber


class HasCompanyCRNControllerSpec extends ControllerSpecBase with MockitoSugar with MockValidationHelper {
  private val schemeName = None

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new HasCRNFormProvider()
  private val form = formProvider("messages__hasCompanyNumber__error__required", "test company name")
  private val index = Index(0)
  private val srn = None

  private val viewModel = CommonFormWithHintViewModel(
    controllers.register.trustees.company.routes.HasCompanyCRNController.onSubmit(NormalMode, index, OptionalSchemeReferenceNumber(srn)),
    title = Message("messages__hasCRN", Message("messages__theCompany").resolve),
    heading = Message("messages__hasCRN", "test company name"),
    hint = Some(Message("messages__hasCompanyNumber__p1"))
  )

  private def getTrusteeCompanyPlusCrn(hasCrnValue: Boolean): FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name"),
          "hasCrn" -> hasCrnValue,
          "noCrnReason" -> "crn number is not present",
          "companyRegistrationNumber" -> Json.obj(
            "value" -> "123456789",
            "isEditadble" -> true
          )
        )
      )
    ))
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany): HasCompanyCRNController =
    new HasCompanyCRNController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyCRNController" must {

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
      FakeUserAnswersService.verify(HasCompanyCRNId(index), true)

    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "if user changes answer from yes to no then clean up should take place on crn number" in {
      FakeUserAnswersService.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = controller(getTrusteeCompanyPlusCrn(hasCrnValue = true)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(HasCompanyCRNId(index), false)
      FakeUserAnswersService.verifyNot(CompanyEnterCRNId(index))
    }

    "if user changes answer from no to yes then clean up should take place on no crn reason" in {
      FakeUserAnswersService.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getTrusteeCompanyPlusCrn(hasCrnValue = false)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(HasCompanyCRNId(index), true)
      FakeUserAnswersService.verifyNot(CompanyNoCRNReasonId(index))
    }
  }
}

