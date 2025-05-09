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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.TrusteeKindFormProvider
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId}
import models.register.trustees.TrusteeKind
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import views.html.register.trustees.trusteeKind

class TrusteeKindControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new TrusteeKindFormProvider()
  val form: Form[TrusteeKind] = formProvider()
  val index: Index = Index(0)


  private val view = injector.instanceOf[trusteeKind]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): TrusteeKindController =
    new TrusteeKindController(messagesApi, FakeUserAnswersService, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider,
      controllerComponents,
      view)

  val submitUrl: Call = controllers.register.trustees.routes.TrusteeKindController.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)

  def viewAsString(form: Form[?] = form): String = view(form,  NormalMode, Index(0), None, submitUrl, EmptyOptionalSchemeReferenceNumber)(fakeRequest, messages).toString

  "TrusteeKind Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        "trustees" -> Json.arr(Json.obj(TrusteeKindId.toString -> JsString(TrusteeKind.values.head.toString))))
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(TrusteeKind.values.head))
    }

    "save the data and redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", TrusteeKind.options.head.value))

      val result = controller().onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.userAnswer.get(TrusteeKindId(0)) mustBe Some(TrusteeKind.values.head)
    }

    "save IsTrusteeNewId flag and redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", TrusteeKind.options.head.value))

      val result = controller().onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.userAnswer.get(IsTrusteeNewId(0)) mustBe Some(true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", TrusteeKind.options.head.value))
      val result = controller(dontGetAnyData).onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
