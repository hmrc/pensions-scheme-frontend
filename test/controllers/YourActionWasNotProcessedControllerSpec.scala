/*
 * Copyright 2021 HM Revenue & Customs
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
import identifiers.SchemeNameId
import models.NormalMode
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.yourActionWasNotProcessed

class YourActionWasNotProcessedControllerSpec extends ControllerSpecBase {

  private val view = injector.instanceOf[yourActionWasNotProcessed]
  private val schemeName = "test scheme"
  private val data = UserAnswers().set(SchemeNameId)(schemeName).asOpt.get.dataRetrievalAction

  private def controller: YourActionWasNotProcessedController = new YourActionWasNotProcessedController(
    messagesApi, controllerComponents, FakeAuthAction, data, new DataRequiredActionImpl, view)

  private val redirectUrl = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None)

  private def viewAsString() = view(Some(schemeName), redirectUrl)(fakeRequest, messages).toString

  "YourActionWasNotProcessed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller.onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}




