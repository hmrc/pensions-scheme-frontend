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

package controllers.register.establishers.partnership.partner

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.establishers.partnership.partner.routes.PartnerNameController
import models.{Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.register.establishers.partnership.partner.whatYouWillNeed

class WhatYouWillNeedPartnerControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val partnershipName = "test partnership name"

  private val view = injector.instanceOf[whatYouWillNeed]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership): WhatYouWillNeedPartnerController =
    new WhatYouWillNeedPartnerController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )

  private def href: Call = PartnerNameController.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, None)

  private def viewAsString(): String = view(None, None, partnershipName, href)(fakeRequest, messages).toString

  "WhatYouWillNeedPartnerControllerSpec" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, Index(0), None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

