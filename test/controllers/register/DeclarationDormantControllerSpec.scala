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

package controllers.register

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationDormantFormProvider
import identifiers.register.{DeclarationDormantId, SchemeDetailsId}
import models.register.{DeclarationDormant, SchemeDetails, SchemeType}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.declarationDormant

class DeclarationDormantControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new DeclarationDormantFormProvider()
  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): DeclarationDormantController =
    new DeclarationDormantController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  val schemeName = "Test Scheme Name"

  val validData: JsObject = Json.obj(
    SchemeDetailsId.toString -> SchemeDetails(schemeName, SchemeType.SingleTrust),
    DeclarationDormant.toString -> DeclarationDormant.values.head.toString
  )

  def viewAsString(form: Form[_] = form): String = declarationDormant(frontendAppConfig, form, schemeName)(fakeRequest, messages).toString

  "DeclarationDormant Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(new FakeDataRetrievalAction(Some(validData))).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        SchemeDetailsId.toString -> SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
        DeclarationDormantId.toString -> JsString(DeclarationDormant.values.head.toString)
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(DeclarationDormant.values.head))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationDormant.options.head.value))

      val result = controller(new FakeDataRetrievalAction(Some(validData))).onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(new FakeDataRetrievalAction(Some(validData))).onSubmit(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationDormant.options.head.value))
          val result = controller(dontGetAnyData).onSubmit(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
      "scheme details cannot be retrieved" when {
        "GET" in {
          val result = controller(getEmptyData).onPageLoad(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

          val result = controller(getEmptyData).onSubmit(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
