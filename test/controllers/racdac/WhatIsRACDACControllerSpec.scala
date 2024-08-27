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

package controllers.racdac

import connectors.PensionAdministratorConnector
import controllers.ControllerSpecBase
import controllers.actions._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.racdac.whatIsRACDAC

import scala.concurrent.Future

class WhatIsRACDACControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val pensionAdministratorConnector: PensionAdministratorConnector = mock[PensionAdministratorConnector]

  def onwardRoute: Call = controllers.routes.SessionExpiredController.onPageLoad

  private val psaName = "Psa Name"
  private val view = injector.instanceOf[whatIsRACDAC]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatIsRACDACController =
    new WhatIsRACDACController(messagesApi,
      FakeAuthAction,
      pensionAdministratorConnector,
      dataRetrievalAction,
      FakeAllowAccessProvider(srn),
      controllerComponents,
      view
    )

  def viewAsString(): String = view(psaName)(fakeRequest, messages).toString

  "WhatIsRACDACController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        when(pensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

