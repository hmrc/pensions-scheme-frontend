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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasCrnFormProvider
import identifiers.register.establishers.company.{HasCompanyNumberId, HasCompanyVATId, IsDetailsCompleteId}
import models.{Index, NormalMode}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsNull, JsValue}
import play.api.test.Helpers._
import services.UserAnswersService
import utils.{FakeNavigator, MockValidationHelper}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasCrn

import scala.concurrent.Future

class HasCompanyNumberControllerSpec extends ControllerSpecBase with MockitoSugar with MockValidationHelper {
  private val schemeName = None
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()
  val formProvider = new HasCrnFormProvider()
  val form = formProvider("messages__hasCompanyNumber__error__required","test company name")
  val index = Index(0)
  val srn = None
  val postCall = controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit(NormalMode, srn, index)

  lazy val mockUserAnswersService = mock[UserAnswersService]

  val viewModel = CommonFormWithHintViewModel(
    controllers.register.establishers.company.routes.HasCompanyNumberController.onSubmit(NormalMode, srn, index),
    title = Message("messages__hasCompanyNumber__title"),
    heading = Message("messages__hasCompanyNumber__h1", "test company name"),
    hint = Some(Message("messages__hasCompanyNumber__p1"))
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): HasCompanyNumberController =
    new HasCompanyNumberController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form) = hasCrn(frontendAppConfig, form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasCompanyNumberController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, None, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(JsNull))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, None, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      val captor = ArgumentCaptor.forClass(classOf[JsValue])
      verify(mockUserAnswersService).upsert(eqTo(NormalMode), eqTo(None), captor.capture())(any(), any(), any())

      captor.getValue must containJson (HasCompanyNumberId(0), true)
      captor.getValue must containJson (IsDetailsCompleteId(0), false)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, None, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}

