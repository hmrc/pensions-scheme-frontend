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

import connectors.PSANameCacheConnector
import controllers.actions._
import models.NormalMode
import models.requests.AuthenticatedRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import views.html.whatYouWillNeed

import scala.concurrent.Future

class WhatYouWillNeedControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)

  private val fakePsaNameCacheConnector = mock[PSANameCacheConnector]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedController =
    new WhatYouWillNeedController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      fakePsaNameCacheConnector,
      ApplicationCrypto
    )

  val fakeRequestWithExternalId = AuthenticatedRequest(fakeRequest, "ext-id", PsaId("A2000000"))

  def viewAsString(): String = whatYouWillNeed(frontendAppConfig)(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to Scheme details page if the email exists for an external Id" in {
        when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
          Future.successful(Some(Json.obj("psaName" -> "test name", "psaEmail" -> "test@test.com")))).thenReturn(
          Future.successful(None))

        val result = controller().onSubmit(fakeRequestWithExternalId)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to Scheme details page if the email exists for a psa id" in {
        val fakeRequestWithExternalId = AuthenticatedRequest(fakeRequest, "ext-id", PsaId("A2000000"))

        when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
          Future.successful(None)).thenReturn(
          Future.successful(Some(Json.obj("psaName" -> "test name", "psaEmail" -> "test@test.com")
        )))

        val result = controller().onSubmit(fakeRequestWithExternalId)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to Scheme details page if the psa name does not exist" in {
        when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
          Future.successful(None))
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to Need Contact page if psaName exists but email does not exist" in {
        when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
          Future.successful(Some(Json.obj("psaName" -> "test name"))))
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.routes.NeedContactController.onPageLoad.url)
      }
    }
  }
}
