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
import controllers.actions.FakeDataRetrievalAction
import identifiers.SchemeNameId
import identifiers.register.trustees.individual.TrusteeNameId
import models.person.{PersonDetails, PersonName}
import models.{Index, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.mvc.{Action, AnyContent, Call, Result}
import viewmodels.Message
import views.html.register.trustees.individual.WhatYouWillNeedIndividualContactDetailsView

import scala.concurrent.Future

class WhatYouWillNeedIndividualContactDetailsControllerSpec extends ControllerSpecBase {

  val trusteeName =  PersonName("Test", "Name")

  val schemeNameAndTrusteeDetailsData = Json.obj(
    SchemeNameId.toString -> "Test Scheme Name",
    "trustees" -> Json.arr(
      Json.obj(
        TrusteeNameId.toString -> trusteeName
      )
    )
  )

  def controller: WhatYouWillNeedIndividualContactDetailsController =
    applicationBuilder(
      new FakeDataRetrievalAction(Some(schemeNameAndTrusteeDetailsData)),
      featureSwitchEnabled = true
    ).build()
      .injector
      .instanceOf[WhatYouWillNeedIndividualContactDetailsController]

  val index = Index(0)

  "WhatYouWillNeedIndividualContactDetailsController" must {
    "in Subscription" must {
      "on a GET" in {
        val result: Future[Result] = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

        val expectedSchemeName: Option[String] = Some("Test Scheme Name")
        val expectedRedirectLocation: Call = routes.TrusteeEmailController.onPageLoad(NormalMode, index, None)
        val expectedHeading = Message("messages__whatYouWillNeedTrusteeIndividualContact__h1", trusteeName.fullName)
        val expectedView = WhatYouWillNeedIndividualContactDetailsView(frontendAppConfig, expectedSchemeName, expectedRedirectLocation, None, expectedHeading)(fakeRequest, messages).toString

        status(result) mustEqual OK
        contentAsString(result) mustBe expectedView
      }

    }

    "in Variance" must {
      "on a GET" in {
        val srn = Some("1234567890")
        val result: Future[Result] = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

        val expectedSchemeName: Option[String] = Some("Test Scheme Name")
        val expectedRedirectLocation: Call = routes.TrusteeEmailController.onPageLoad(UpdateMode, index, srn)
        val expectedHeading = Message("messages__whatYouWillNeedTrusteeIndividualContact__h1", trusteeName.fullName)
        val expectedView = WhatYouWillNeedIndividualContactDetailsView(frontendAppConfig, expectedSchemeName, expectedRedirectLocation, srn, expectedHeading)(fakeRequest, messages).toString

        status(result) mustEqual OK
        contentAsString(result) mustBe expectedView
      }

    }

  }
}
