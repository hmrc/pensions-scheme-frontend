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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.register.establishers.individual.routes.EstablisherEmailController
import models.person.PersonName
import models.{Mode, NormalMode, UpdateMode}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.UserAnswers
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

class WhatYouWillNeedIndividualContactDetailsControllerSpec extends ControllerSpecBase {

  private val establisherName = PersonName("Test", "Name")
  private val index = 0
  private val srn = Some("srn")

  private def onwardRoute(mode: Mode, srn: Option[String]): Call = EstablisherEmailController.onPageLoad(mode, index, srn)

  private val view = injector.instanceOf[whatYouWillNeedContactDetails]

  private def viewAsString(mode: Mode = NormalMode, srn: Option[String] = None): String = view(
    None, onwardRoute(mode, srn), srn, establisherName.fullName, Message("messages__theIndividual"))(fakeRequest, messages).toString

  "WhatYouWillNeedIndividualContactDetailsController" when {
    "in Subscription" must {
      "return the correct view on a GET" in {
        running(_.overrides(
          modules(UserAnswers().establishersIndividualName(index, establisherName).dataRetrievalAction): _*
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedIndividualContactDetailsController]
          val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }

    "in Variation" must {
      "return the correct view on a GET" in {
        running(_.overrides(
          modules(UserAnswers().establishersIndividualName(index, establisherName).dataRetrievalAction): _*
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedIndividualContactDetailsController]
          val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, srn)
        }
      }
    }
  }
}
