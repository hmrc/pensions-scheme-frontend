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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import forms.ReasonFormProvider
import identifiers.register.establishers.company.director.DirectorNoUTRReasonId
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, OptionalSchemeReferenceNumber}
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class DirectorNoUTRReasonControllerSpec extends ControllerSpecBase {

  import DirectorNoUTRReasonControllerSpec._

  "DirectorNoUTRReasonController" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK and the correct view for a GET where valid reason given" in {
      val validData = validCompanyDirectorData("noUtrReason" -> "new reason")

      val dataRetrievalAction = new FakeDataRetrievalAction(Some(validData))
      val result = controller(dataRetrievalAction = dataRetrievalAction).onPageLoad(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form = form.fill("new reason"))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "new reason"))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(DirectorNoUTRReasonId(establisherIndex, directorIndex), "new reason")
    }

    "return a Bad Request when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", ""))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
    }

    "render the same page with invalid error message when invalid characters are entered" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "<>?:-{}<>,/.,/;#\";]["))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST

      contentAsString(result) must include("The reason must only include letters, spaces, ampersands (&amp;), grave accents (à), apostrophes, hyphens, full stops and carets (^)")
    }

    "render the same page with maxlength error message when invalid characters are entered" ignore {
      val reason = "a"*161
      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", reason))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(messages("messages__reason__error_maxLength"))
    }

    "render the same page with required error message when nothing is entered" ignore {
      val postRequest = fakeRequest

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(messages("messages__reason__error_utrRequired", "first last"))
    }
  }

}

object DirectorNoUTRReasonControllerSpec extends ControllerSpecBase {
  private val schemeName = None

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val name = "test director name"
  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", name)
  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val srn = None
  private val postCall = controllers.register.establishers.company.director.routes.DirectorNoUTRReasonController.onSubmit(NormalMode, establisherIndex, directorIndex, OptionalSchemeReferenceNumber(srn))
  private val viewModel = ReasonViewModel(
    postCall,
    title = Message("messages__noDirectorUtr__title"),
    heading = Message("messages__whyNoUTR", "first last"),
    srn = EmptyOptionalSchemeReferenceNumber
  )
  private val view = injector.instanceOf[reason]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompanyDirectorWithDirectorName): DirectorNoUTRReasonController =
    new DirectorNoUTRReasonController(
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

  private def viewAsString(form: Form[?] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString
}

