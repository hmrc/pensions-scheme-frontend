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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions.FakeDataRetrievalAction
import forms.ReasonFormProvider
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNameId}
import models.person.{PersonDetails, PersonName}
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.Future

class TrusteeNoUTRReasonControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", "test director name")
  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val postCall = controllers.register.trustees.individual.routes.TrusteeNoUTRReasonController.onSubmit(NormalMode, Index(0), None)
  private val viewModel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__noGenericUtr__title", Message("messages__theTrustee")),
    heading = Message("messages__noGenericUtr__heading", "Test Name"),
    srn = None
  )

  private def viewAsString(form: Form[_] = form) = reason(frontendAppConfig, form, viewModel, None)(fakeRequest, messages).toString

  "TrusteeNoUTRReasonController" must {
    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getMandatoryTrustee).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val result = controller.onPageLoad(NormalMode, Index(0), None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "return OK and the correct view for a GET where valid reason given" in {
      val trusteeDataWithNoUTRReasonAnswer = new FakeDataRetrievalAction(Some(validTrusteeData("noUtrReason" -> "blah")))

      println(s"\n\n\n${validTrusteeData("noUtrReason" -> "blah")}\n\n\n")
      val app = applicationBuilder(trusteeDataWithNoUTRReasonAnswer).build()

      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]

      val result = controller.onPageLoad(NormalMode, Index(0), None)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

//    "redirect to the next page when valid data is submitted" in {
//      val app = applicationBuilder(getMandatoryTrustee)
//        .overrides(
//          bind[UserAnswersService].toInstance(mockUserAnswersService)
//        ).build()
//
//      val validData = Json.obj(
//        "trustees" -> Json.arr(
//          Json.obj(
//            TrusteeNameId.toString ->
//              PersonName("Test", "Name")
//          )
//        )
//      )
//
//      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))
//
//      val controller = app.injector.instanceOf[TrusteeNoUTRReasonController]
//
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "blah"))
//
//      val result = controller.onSubmit(NormalMode, Index(0), None)(postRequest)
//
//      status(result) mustBe SEE_OTHER
//
//      redirectLocation(result) mustBe Some(onwardRoute.url)
//
//      app.stop()
//    }

//    "return a Bad Request when invalid data is submitted" in {
//      val app = applicationBuilder(getMandatoryTrustee).build()
//
//      val controller = app.injector.instanceOf[TrusteeHasNINOController]
//
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", ""))
//
//      val boundForm = form.bind(Map("value" -> "invalid value"))
//
//      val result = controller.onSubmit(NormalMode, Index(0), None)(postRequest)
//
//      status(result) mustBe BAD_REQUEST
//
//      contentAsString(result) mustBe viewAsString(boundForm)
//
//      app.stop()
//    }
  }
}
