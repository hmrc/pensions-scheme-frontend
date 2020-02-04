/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.actions.{DataRetrievalAction, FakeAuthAction}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator

class ContinueRegistrationControllerSpec extends ControllerSpecBase {

  import ContinueRegistrationControllerSpec._

  "ContinueRegistrationController" when {
    "asked to continue" must {
      "redirect to the next page" in {
        val result = controller(getEmptyData).continue()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(nextPage.url)
      }
    }
  }

}

object ContinueRegistrationControllerSpec {

  implicit val global = scala.concurrent.ExecutionContext.Implicits.global

  def nextPage: Call = Call("GET", "http://www.test.com")

  val fakeNavigator = new FakeNavigator(nextPage)

  def controller(data: DataRetrievalAction): ContinueRegistrationController =
    new ContinueRegistrationController(FakeAuthAction, data, fakeNavigator)

}
