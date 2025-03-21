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

package controllers

import connectors.SessionDataCacheConnector
import controllers.actions.FakeAuthAction
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results
import play.api.test.Helpers._

import scala.concurrent.Future


class LogoutControllerSpec extends ControllerSpecBase with Results with MockitoSugar {

  private val mockSessionDataCacheConnector = mock[SessionDataCacheConnector]

  def logoutController: LogoutController =
    new LogoutController(frontendAppConfig, controllerComponents, FakeAuthAction, mockSessionDataCacheConnector)

  "Logout Controller" must {

    "redirect to feedback survey page for an Individual and clear down session data cache" in {
      when(mockSessionDataCacheConnector.removeAll(any(), any()))
        .thenReturn(Future.successful(Ok))
      val result = logoutController.onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.serviceSignOut)
      verify(mockSessionDataCacheConnector, times(1)).removeAll(any(), any())
    }
  }
}
