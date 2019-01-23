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

import config.FeatureSwitchManagementService
import controllers.actions._
import models.NormalMode
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeFeatureSwitchManagementService
import views.html.whatYouWillNeedWorkingKnowledge

class WhatYouWillNeedWorkingKnowledgeControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.AdviserNameController.onPageLoad(NormalMode)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, isEnabledV2: Boolean = false): WhatYouWillNeedWorkingKnowledgeController =
    new WhatYouWillNeedWorkingKnowledgeController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      new FakeFeatureSwitchManagementService(isEnabledV2),
      dataRetrievalAction
    )

  def viewAsString(): String = whatYouWillNeedWorkingKnowledge(frontendAppConfig, None)(fakeRequest, messages).toString

  "WhatYouWillNeedWorkingKnowledgeController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to working knowledge page when toggle is off" in {
        val result = controller().onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.adviser.routes.WorkingKnowledgeController.onPageLoad(NormalMode).url)
      }

      "redirect to adviser name page when toggle is on" in {
        val result = controller(isEnabledV2 = true).onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

