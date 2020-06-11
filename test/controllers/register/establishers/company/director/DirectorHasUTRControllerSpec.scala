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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.establishers.company.director.DirectorHasUTRId
import models.{Index, NormalMode}
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class DirectorHasUTRControllerSpec extends ControllerSpecBase {

  import DirectorHasUTRControllerSpec._

  "DirectorHasUTRController" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK and the correct view for a GET where question already answered" in {
      val validData = validCompanyDirectorData("hasUtr" -> false)

      val dataRetrievalAction = new FakeDataRetrievalAction(Some(validData))
      val result = controller(dataRetrievalAction = dataRetrievalAction).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form = form.fill(value = false))
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(DirectorHasUTRId(establisherIndex, directorIndex), true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}

object DirectorHasUTRControllerSpec extends ControllerSpecBase {
  private val schemeName = None

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("error", "test company name")
  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val srn = None
  private val postCall = routes.DirectorHasUTRController.onSubmit(NormalMode, establisherIndex, directorIndex, srn)
  private val viewModel = CommonFormWithHintViewModel(
    postCall,
    title = Message("messages__hasUTR", Message("messages__theDirector")),
    heading = Message("messages__hasUTR", "first last"),
    hint = Some(Message("messages__hasUtr__p1"))
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompanyDirectorWithDirectorName): DirectorHasUTRController =
    new DirectorHasUTRController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString
}

