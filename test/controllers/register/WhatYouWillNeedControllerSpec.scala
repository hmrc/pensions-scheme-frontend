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

import controllers.actions._
import play.api.test.Helpers._
import views.html.register.whatYouWillNeed
import controllers.ControllerSpecBase
import models.NormalMode
import play.api.mvc.Call

class WhatYouWillNeedControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedController =
    new WhatYouWillNeedController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction)

  def viewAsString(): String = whatYouWillNeed(frontendAppConfig)(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Scheme details page on a POST" in {
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url)
    }
  }
}




