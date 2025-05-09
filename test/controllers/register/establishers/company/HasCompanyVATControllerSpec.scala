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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.company.HasCompanyVATId
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, OptionalSchemeReferenceNumber}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, MockValidationHelper}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class HasCompanyVATControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach
  with OptionValues with MockValidationHelper {

  private val schemeName = None

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  val formProvider = new HasReferenceNumberFormProvider()
  val form = formProvider("messages__hasCompanyVat__error__required", "test company name")
  val index = Index(0)
  val srn = None
  val postCall = controllers.register.establishers.company.routes.HasCompanyVATController.onSubmit(NormalMode, OptionalSchemeReferenceNumber(srn), index)

  val viewModel = CommonFormWithHintViewModel(
    controllers.register.establishers.company.routes.HasCompanyVATController.onSubmit(NormalMode, OptionalSchemeReferenceNumber(srn), index),
    title = Message("messages__hasVAT", Message("messages__theCompany").resolve),
    heading = Message("messages__hasVAT", "test company name")
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): HasCompanyVATController =
    new HasCompanyVATController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      view,
      controllerComponents
    )

  private def viewAsString(form: Form[?] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyVatController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(HasCompanyVATId(index), true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}

