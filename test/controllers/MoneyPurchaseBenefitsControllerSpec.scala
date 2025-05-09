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

package controllers

import controllers.actions._
import forms.MoneyPurchaseBenefitsFormProvider
import identifiers.{MoneyPurchaseBenefitsId, SchemeNameId}
import models.MoneyPurchaseBenefits._
import models._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import views.html.moneyPurchaseBenefits

class MoneyPurchaseBenefitsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new MoneyPurchaseBenefitsFormProvider()
  val form: Form[MoneyPurchaseBenefits] = formProvider()
  val schemeName = "Test Scheme Name"
  val postCall: (Mode, OptionalSchemeReferenceNumber) => Call = routes.MoneyPurchaseBenefitsController.onSubmit

  private val view = injector.instanceOf[moneyPurchaseBenefits]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs): MoneyPurchaseBenefitsController =
    new MoneyPurchaseBenefitsController(messagesApi, FakeUserAnswersService, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider, controllerComponents, view)

  def viewAsString(form: Form[?] = form): String = view(form, NormalMode, Some(schemeName), postCall(NormalMode, EmptyOptionalSchemeReferenceNumber),
                                                                                                 EmptyOptionalSchemeReferenceNumber)(fakeRequest, messages).toString

  private val validData = Json.obj(
    SchemeNameId.toString -> schemeName,
    MoneyPurchaseBenefitsId.toString -> MoneyPurchaseBenefits.Collective.toString
  )

  "MoneyPurchaseBenefits Controller" must {

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(Collective))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", MoneyPurchaseBenefits.values.head.toString))
      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
