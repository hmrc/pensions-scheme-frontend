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

import controllers.ControllerSpecBase
import controllers.actions._
import play.api.mvc.Call
import play.api.test.Helpers._
import viewmodels.{JourneyTaskList, JourneyTaskListSection, Link}
import views.html.schemeTaskList

class SchemeTaskListControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val jtlSection = JourneyTaskListSection(None, Link("linkText", "linkTarget"), None)
  val journeyTL = JourneyTaskList(jtlSection, Seq(jtlSection), Seq(jtlSection), jtlSection, None)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  def viewAsString(): String =
    schemeTaskList(
      frontendAppConfig, journeyTL
    )(fakeRequest, messages).toString()

  "SchemeTaskList Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}
