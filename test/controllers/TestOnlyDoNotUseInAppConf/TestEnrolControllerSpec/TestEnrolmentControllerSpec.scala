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

import connectors.{PSANameCacheConnector, TestOnlyCacheConnector}
import controllers.actions._
import controllers.testOnlyDoNotUseInAppConf.TestEnrolController
import forms.mappings.Mappings
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import views.html.testOnlyDoNotUseInAppConf.testEnrol

import scala.concurrent.Future

class TestEnrolControllerSpec extends ControllerSpecBase with Mappings with MockitoSugar {

  def apply(): Form[String] =
    Form(
      "value" -> text("messages__enrolment__error__name_invalid")
    )

  val formProvider = apply()
  val psaNameCacheConnector = mock[PSANameCacheConnector]
  val testOnlyCacheConnector = mock[TestOnlyCacheConnector]

  def onwardRoute: Call = controllers.routes.WhatYouWillNeedController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): TestEnrolController =
    new TestEnrolController(frontendAppConfig,
      messagesApi,
      psaNameCacheConnector,
      testOnlyCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  def viewAsString(form: Form[_] = formProvider): String = testEnrol(frontendAppConfig, form)(fakeRequest, messages).toString

  "TestEnrol Controller" must {

    "return OK and the correct view for a GET" in {
      when(testOnlyCacheConnector.dropCollection(any())(any(), any())).thenReturn(Future.successful(Ok))
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return OK when valid data is submitted" in {
      when(psaNameCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Json.obj()))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("psaName", "company"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
