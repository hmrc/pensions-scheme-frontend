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
import forms.HasPAYEFormProvider
import identifiers.register.establishers.company.HasCompanyPAYEId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class HasCompanyPAYEControllerSpec  extends ControllerSpecBase {
  private val schemeName = None
  def onwardRoute: Call = controllers.register.establishers.company.routes.CompanyEnterPAYEController.onPageLoad (NormalMode, index, None)

  val formProvider = new HasPAYEFormProvider()
  val form = formProvider("messages__companyPayeRef__error__required","test company name")
  val index = Index(0)
  val srn = None
  val postCall = controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(NormalMode, srn, index)

  val viewModel = CommonFormWithHintViewModel(
    controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(NormalMode, srn, index),
    title = Message("messages__hasPAYE", Message("messages__theCompany").resolve),
    heading = Message("messages__hasPAYE", "test company name"),
    hint = Some(Message("messages__hasPaye__p1")),
    formFieldName = Some("hasPaye")
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): HasCompanyPAYEController =
    new HasCompanyPAYEController(
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
      controllerComponents,
      global
    )

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyPAYEController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, srn, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("hasPaye", "true"))

      val result = controller().onSubmit(NormalMode, srn, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(HasCompanyPAYEId(index), true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("hasPAYE", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, srn, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}

