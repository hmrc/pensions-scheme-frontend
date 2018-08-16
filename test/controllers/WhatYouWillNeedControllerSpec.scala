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
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
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
      new FakeNavigator(onwardRoute)
    )

  def viewAsString(): String = whatYouWillNeed(frontendAppConfig)(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Scheme details page if the email exists on a POST" in {
      when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
        Future.successful(Some(Json.obj("psaName" -> "test name", "psaEmail" -> "test@test.com"))))
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Scheme details page if the psa name does not exist on a POST" in {
      when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
        Future.successful(None))
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Need Contact page if psaName exists but email does not exist on a POST" in {
      when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(
        Future.successful(Some(Json.obj("psaName" -> "test name"))))
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.routes.NeedContactController.onPageLoad.url)
    }
  }

}
