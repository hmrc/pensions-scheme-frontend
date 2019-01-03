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

package controllers

import connectors.{FakeUserAnswersCacheConnector, PSANameCacheConnector}
import controllers.actions._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto

class WhatYouWillNeedControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val fakePsaNameCacheConnector = mock[PSANameCacheConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedController =
    new WhatYouWillNeedController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      fakePsaNameCacheConnector,
      applicationCrypto,
      FakeUserAnswersCacheConnector
    )

  override def beforeEach(): Unit = {
    reset(fakePsaNameCacheConnector)
    super.beforeEach()
  }

  "WhatYouWillNeed Controller " when {
    "on a POST" must {
      "redirect to Scheme details page" in {
        val result = controller().onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.SchemeTaskListController.onPageLoad().url)
      }
    }
  }
}
