/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasUTRFormProvider
import identifiers.register.establishers.individual.EstablisherHasNINOId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class EstablisherHasNINOControllerSpec extends ControllerSpecBase {
  private val schemeName   = None
  private val name = "Test Name"
  private def onwardRoute: Call  = controllers.routes.IndexController.onPageLoad()
  private val formProvider = new HasUTRFormProvider()
  private val form         = formProvider("messages__genericHasNino__error__required", name)
  private val index        = Index(0)
  private val srn          = None
  private val postCall     = controllers.register.establishers.individual.routes.EstablisherHasNINOController.onSubmit(NormalMode, index, srn)
  private val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__hasNINO", Message("messages__theIndividual")),
    heading = Message("messages__hasNINO", name),
    hint = None
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherIndividual): EstablisherHasNINOController =
    new EstablisherHasNINOController(
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
      stubMessagesControllerComponents()
    )

  private def viewAsString(form: Form[_] = form): String = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "EstablisherHasNINOController" must {

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
      FakeUserAnswersService.verify(EstablisherHasNINOId(index), value = true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm   = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}
