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

package controllers

import controllers.actions._
import forms.register.DeclarationFormProvider
import models.NormalMode
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.variationDeclaration

class VariationDeclarationControllerSpec extends ControllerSpecBase {

  private val formProvider = new DeclarationFormProvider()
  private val form = formProvider()
  val schemeName = "Test Scheme Name"
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs): VariationDeclarationController =
    new VariationDeclarationController(frontendAppConfig, messagesApi, new FakeNavigator(onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  private def viewAsString() = variationDeclaration(frontendAppConfig, form, Some(schemeName))(fakeRequest, messages).toString

  "VariationDeclarationController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

      "redirect to the next page for a POST" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("agree", "agreed"))
        val result = controller().onSubmit()(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}




