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
import controllers.actions._
import forms.HasReferenceNumberFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.{TrusteeHasUTRId, TrusteeNameId, TrusteeNoUTRReasonId, TrusteeUTRId}
import models.person.PersonName
import models.{Index, NormalMode, person}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasUtr

class TrusteeHasUTRControllerSpec extends ControllerSpecBase {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new HasReferenceNumberFormProvider()
  private val form = formProvider("messages__hasUtr__error__required", "test name")
  private val index = Index(0)
  private val srn = None
  private val utr = "test-utr"
  private val noUtr = "no utr"
  private val postCall = controllers.register.trustees.individual.routes.TrusteeHasUTRController.onSubmit(NormalMode, index, srn)
  private val viewModel = CommonFormWithHintViewModel(
    postCall = postCall,
    title = Message("messages__hasUTR", Message("messages__theIndividual").resolve),
    heading = Message("messages__hasUTR", "first Last"),
    hint = Some(Message("messages__hasUtr__p1")),
    srn = srn
  )

  private val trusteeIndividualData = UserAnswers().set(TrusteeNameId(0))(PersonName("first", "Last")).asOpt.value

  private def getTrusteeIndividualDataWithUtr(hasUtrValue:Boolean): FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          TrusteeNameId.toString -> person.PersonName("first", "last"),
          TrusteeHasUTRId.toString -> hasUtrValue,
          TrusteeUTRId.toString -> utr,
          TrusteeNoUTRReasonId.toString -> noUtr
        )
      )
    ))
  )

  private def controller(dataRetrievalAction: DataRetrievalAction =
                         trusteeIndividualData.dataRetrievalAction, isHnsEnabled: Boolean = false): TrusteeHasUTRController =
    new TrusteeHasUTRController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_] = form): String = hasUtr(frontendAppConfig, form, viewModel, None)(fakeRequest, messages).toString

  "TrusteeHasUTRController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.userAnswer.get(TrusteeHasUTRId(index)).value mustEqual true
    }

    "redirect to the session expired page when no trustee name has been added" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(getEmptyData).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "clean up utr number, if user changes answer from yes to no" in {
     val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = controller(getTrusteeIndividualDataWithUtr(true)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER

      FakeUserAnswersService.userAnswer.get(TrusteeHasUTRId(index)).value mustEqual false
      FakeUserAnswersService.userAnswer.get(TrusteeUTRId(index)) mustBe None
    }

    "clean up no utr reason, if user changes answer from no to yes" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getTrusteeIndividualDataWithUtr(false)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.userAnswer.get(TrusteeHasUTRId(index)).value mustEqual true
      FakeUserAnswersService.userAnswer.get(TrusteeNoUTRReasonId(index)) mustBe None
    }
  }
}
