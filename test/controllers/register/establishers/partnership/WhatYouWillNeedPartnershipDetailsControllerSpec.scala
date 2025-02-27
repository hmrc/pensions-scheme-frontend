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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, PartnershipDetails}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.whatYouWillNeedPartnershipDetails

class WhatYouWillNeedPartnershipDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val index = 0
  private val establisherPartnership = PartnershipDetails("partnership Name")

  def onwardRoute: Call = routes.PartnershipHasUTRController.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)

  private val view = injector.instanceOf[whatYouWillNeedPartnershipDetails]

  def viewAsString(): String = view(
    None, onwardRoute, establisherPartnership.name, EmptyOptionalSchemeReferenceNumber)(fakeRequest, messages).toString

  "WhatYouWillNeedPartnershipDetailsController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(
          bind[AuthAction].toInstance(FakeAuthAction),
          bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
          bind[DataRetrievalAction].toInstance(UserAnswers().establisherPartnershipDetails(Index(0), establisherPartnership).dataRetrievalAction)
        )) { app =>
          val controller = app.injector.instanceOf[WhatYouWillNeedPartnershipDetailsController]
          val result = controller.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString()
        }
      }
    }
  }
}

