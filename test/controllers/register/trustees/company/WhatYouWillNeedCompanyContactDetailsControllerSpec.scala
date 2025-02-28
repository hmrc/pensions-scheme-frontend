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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.register.trustees.company.routes.CompanyEmailController
import models._
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.UserAnswers
import viewmodels.Message
import views.html.register.whatYouWillNeedContactDetails

class WhatYouWillNeedCompanyContactDetailsControllerSpec extends ControllerSpecBase {

  private val trusteeName = CompanyDetails("Test Company")
  private val srn = Some(SchemeReferenceNumber("srn"))

  private def onwardRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): Call = CompanyEmailController.onPageLoad(mode, Index(0), OptionalSchemeReferenceNumber(srn))
  private val view = injector.instanceOf[whatYouWillNeedContactDetails]
  private def viewAsString(mode: Mode = NormalMode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): String = view(
    None, onwardRoute(mode, OptionalSchemeReferenceNumber(srn)), OptionalSchemeReferenceNumber(srn), trusteeName.companyName, Message("messages__theCompany"))(fakeRequest, messages).toString

  "WhatYouWillNeedCompanyContactDetailsController" when {
    "in Subscription" must {
      "return the correct view on a GET" in {
        running(_.overrides(
          modules(UserAnswers().trusteesCompanyDetails(Index(0), trusteeName).dataRetrievalAction): _*
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedCompanyContactDetailsController]
          val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }

    "in Variation" must {
      "return the correct view on a GET" in {
        running(_.overrides(
          modules(UserAnswers().trusteesCompanyDetails(Index(0), trusteeName).dataRetrievalAction): _*
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedCompanyContactDetailsController]
          val result = controller.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, OptionalSchemeReferenceNumber(srn))
        }
      }
    }
  }
}

