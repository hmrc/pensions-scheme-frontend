/*
 * Copyright 2018 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.PSANameCacheConnector
import controllers.actions._
import identifiers.{PsaEmailId, PsaNameId}
import models.NormalMode
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import views.html.whatYouWillNeed

import scala.concurrent.Future

class WhatYouWillNeedControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  override def frontendAppConfig: FrontendAppConfig = new GuiceApplicationBuilder().configure(
    conf = "features.is-hub-enabled" -> false
  ).build().injector.instanceOf[FrontendAppConfig]

  def onwardRoute: Call = controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)

  private val fakePsaNameCacheConnector = mock[PSANameCacheConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedController =
    new WhatYouWillNeedController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      fakePsaNameCacheConnector,
      applicationCrypto
    )

  val encryptedPsaId: String = applicationCrypto.QueryParameterCrypto.encrypt(PlainText("A0000000")).value

  def viewAsString(): String = whatYouWillNeed(frontendAppConfig)(fakeRequest, messages).toString

  private def verifyFetchCalledOnce = {
    verify(fakePsaNameCacheConnector, times(1)).fetch(eqTo("id"))(any(), any())
    verify(fakePsaNameCacheConnector, times(1)).fetch(eqTo(encryptedPsaId))(any(), any())
  }

  override def beforeEach(): Unit = {
    reset(fakePsaNameCacheConnector)
    super.beforeEach()
  }

  "WhatYouWillNeed Controller" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to Scheme details page" in {
        val result = controller().onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

class WhatYouWillNeedHsControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  override def frontendAppConfig: FrontendAppConfig = new GuiceApplicationBuilder().configure(
    conf = "features.is-hub-enabled" -> true
  ).build().injector.instanceOf[FrontendAppConfig]

  private val fakePsaNameCacheConnector = mock[PSANameCacheConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedController =
    new WhatYouWillNeedController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      fakePsaNameCacheConnector,
      applicationCrypto
    )

  override def beforeEach(): Unit = {
    reset(fakePsaNameCacheConnector)
    super.beforeEach()
  }

  "WhatYouWillNeed Controller (hub and spoke)" when {
    "on a POST" must {
      "redirect to Scheme details page" in {
        val result = controller().onSubmit()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SchemeTaskListController.onPageLoad().url)
      }
    }
  }
}
