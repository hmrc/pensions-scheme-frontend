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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import models.{Index, NormalMode, PartnershipDetails}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.UserAnswers
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

class WhatYouWillNeedPartnershipContactDetailsControllerSpec extends ControllerSpecBase {
  private val index = 0
  private val establisherPartnership = PartnershipDetails("partnership Name")

  def onwardRoute: Call = controllers.register.establishers.company.routes.CompanyEmailController.onPageLoad(NormalMode, None, Index(0))
  private val view = injector.instanceOf[whatYouWillNeedContactDetails]
  def viewAsString(): String = view(
    None,
    controllers.register.establishers.partnership.routes.PartnershipEmailController.onPageLoad(NormalMode, index, None),
    None,
    establisherPartnership.name,
    Message("messages__thePartnership"))(fakeRequest, messages).toString

  "WhatYouWillNeedPartnershipContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(
          bind[AuthAction].toInstance(FakeAuthAction),
          bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
          bind[DataRetrievalAction].toInstance(UserAnswers().establisherPartnershipDetails(index, establisherPartnership).dataRetrievalAction)
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedPartnershipContactDetailsController]
          val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }
  }
}

