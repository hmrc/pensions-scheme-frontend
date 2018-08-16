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

import connectors.{FakeDataCacheConnector, PSANameCacheConnector}
import controllers.actions._
import identifiers.TypedIdentifier
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import views.html.whatYouWillNeed

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  object fakePsaNameCacheConnector extends PSANameCacheConnector(
    frontendAppConfig,
    mock[WSClient],
    injector.instanceOf[ApplicationCrypto]
  ) with FakeDataCacheConnector {

    override def fetch(cacheId: String)(implicit
                                        ec: ExecutionContext,
                                        hc: HeaderCarrier): Future[Option[JsValue]] = Future.successful(Some(Json.obj("psaName" -> "Test",
      "psaEmail" -> "email@test.com")))

    override def upsert(cacheId: String, value: JsValue)
                       (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = Future.successful(value)

    override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
                                                (implicit
                                                 ec: ExecutionContext,
                                                 hc: HeaderCarrier
                                                ): Future[JsValue] = ???
  }


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

    "redirect to Scheme details page on a POST" in {
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }

}
