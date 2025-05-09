/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.*
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, PartnershipDetails}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.{UserAnswerOps, UserAnswers}
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

class WhatYouWillNeedPartnershipContactDetailsControllerSpec extends ControllerSpecBase {
  private val trusteePartnership = PartnershipDetails("partnership Name")

  def onwardRoute: Call = controllers.register.trustees.company.routes.CompanyEmailController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)
  private val view = injector.instanceOf[whatYouWillNeedContactDetails]
  def viewAsString(): String = view(
    None,
    controllers.register.trustees.partnership.routes.PartnershipEmailController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber),
    EmptyOptionalSchemeReferenceNumber,
    trusteePartnership.name,
    Message("messages__thePartnership")
    )(fakeRequest, messages).toString

  "WhatYouWillNeedPartnershipContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(
          bind[AuthAction].toInstance(FakeAuthAction),
          bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
          bind[DataRetrievalAction].toInstance(UserAnswers().trusteePartnershipDetails(Index(0), trusteePartnership).dataRetrievalAction)
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedPartnershipContactDetailsController]
          val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }
  }
}

