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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.register.trustees.partnership.routes.PartnershipEmailController
import models.{Mode, NormalMode, PartnershipDetails, UpdateMode}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.whatYouWillNeedContactDetails

class WhatYouWillNeedPartnershipContactDetailsControllerSpec extends ControllerSpecBase {
  private val trusteeName = PartnershipDetails("Test Partnership")
  private val index = 0
  private val srn = Some("srn")

  private def onwardRoute(mode: Mode, srn: Option[String]): Call = PartnershipEmailController.onPageLoad(mode, index, srn)

  private def viewAsString(mode: Mode = NormalMode, srn: Option[String] = None): String = whatYouWillNeedContactDetails(
    frontendAppConfig, None, onwardRoute(mode, srn), srn, trusteeName.name)(fakeRequest, messages).toString

  "WhatYouWillNeedPartnershipContactDetailsController" when {
    "in Subscription" must {
      "return the correct view on a GET" in {
        running(_.overrides(
          modules(UserAnswers().trusteePartnershipDetails(index, trusteeName).dataRetrievalAction, featureSwitchEnabled = true): _*
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedPartnershipContactDetailsController]
          val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }

    "in Variation" must {
      "return the correct view on a GET" in {
        running(_.overrides(
          modules(UserAnswers().trusteePartnershipDetails(index, trusteeName).dataRetrievalAction, featureSwitchEnabled = true): _*
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedPartnershipContactDetailsController]
          val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, srn)
        }
      }
    }
  }
}

