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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import models.{Index, NormalMode, UpdateMode}
import play.api.test.Helpers._
import play.api.mvc.{Action, AnyContent, Call, Result}
import views.html.register.trustees.individual.whatYouWillNeedIndividualContactDetailsView

import scala.concurrent.Future

class WhatYouWillNeedIndividualContactDetailsControllerSpec extends ControllerSpecBase {

  def controller: WhatYouWillNeedIndividualContactDetailsController =
    applicationBuilder(getMandatorySchemeNameHs, true)
      .build()
      .injector
      .instanceOf[WhatYouWillNeedIndividualContactDetailsController]

  val index = Index(0)

  "WhatYouWillNeedIndividualContactDetailsController" must {
    "in Subscription" must {
      "on a GET" in {
        val result: Future[Result] = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

        val schemeName: Option[String] = Some("Test Scheme Name")
        val expectedRedirectLocation: Call = routes.TrusteeEmailController.onPageLoad(NormalMode, index, None)

        status(result) mustEqual OK
        contentAsString(result) mustBe whatYouWillNeedIndividualContactDetailsView(frontendAppConfig, schemeName, expectedRedirectLocation, None)(fakeRequest, messages).toString
      }

    }

    "in Variance" must {
      "on a GET" in {
        val srn = Some("1234567890")
        val result: Future[Result] = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

        val schemeName: Option[String] = Some("Test Scheme Name")
        val expectedRedirectLocation: Call = routes.TrusteeEmailController.onPageLoad(UpdateMode, index, srn)

        status(result) mustEqual OK
        contentAsString(result) mustBe whatYouWillNeedIndividualContactDetailsView(frontendAppConfig, schemeName, expectedRedirectLocation, srn)(fakeRequest, messages).toString
      }

    }

  }
}
