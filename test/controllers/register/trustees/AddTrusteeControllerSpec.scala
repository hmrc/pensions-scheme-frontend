/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.Form
import play.api.libs.json.JsString
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers.{contentAsString, _}
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.trustees.AddTrusteeId
import models.NormalMode
import views.html.register.trustees.addTrustee
import play.api.libs.json._
import controllers.ControllerSpecBase

class AddTrusteeControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val onwardUrl = routes.AddTrusteeController.onPageLoad(NormalMode).url
  val formProvider = new AddTrusteeFormProvider()
  val schemeName = "Test Scheme Name"
  private val maxTrustees = frontendAppConfig.maxTrustees
  val trusteeCompany = ("Trustee Company" -> onwardUrl)
  val trusteeIndividual = ("Trustee Individual" -> onwardUrl)
  val allTrustees = Seq(trusteeCompany, trusteeIndividual)

  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName) =
    new AddTrusteeController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form, trustees: Seq[(String, String)] = Seq.empty) = addTrustee(frontendAppConfig, form, NormalMode, schemeName, trustees)(fakeRequest, messages).toString

  val testAnswer = "answer"

  "AddTrustee Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoe)))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, Seq(johnDoe))
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
