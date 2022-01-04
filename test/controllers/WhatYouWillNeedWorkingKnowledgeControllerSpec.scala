/*
 * Copyright 2022 HM Revenue & Customs
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
import models.NormalMode
import org.scalatest.BeforeAndAfterEach
import org.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._

import views.html.whatYouWillNeedWorkingKnowledge

class WhatYouWillNeedWorkingKnowledgeControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.AdviserNameController.onPageLoad(NormalMode)

  private val view = injector.instanceOf[whatYouWillNeedWorkingKnowledge]
  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedWorkingKnowledgeController =
    new WhatYouWillNeedWorkingKnowledgeController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      controllerComponents,
      view
    )

  def viewAsString(): String = view(None)(fakeRequest, messages).toString

  "WhatYouWillNeedWorkingKnowledgeController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to adviser name page" in {
        val result = controller().onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

