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

import connectors.DataCacheConnector
import controllers.actions._
import forms.DeleteSchemeFormProvider
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.Results.Ok
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import views.html.deleteScheme

import scala.concurrent.Future

class DeleteSchemeControllerSpec extends ControllerSpecBase with MockitoSugar {

  val formProvider = new DeleteSchemeFormProvider()
  val form: Form[Boolean] = formProvider()
  val schemeName = "Test Scheme Name"
  val fakeDataCacheConnector = mock[DataCacheConnector]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName): DeleteSchemeController =
    new DeleteSchemeController(frontendAppConfig, messagesApi, fakeDataCacheConnector, FakeNavigator, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = deleteScheme(frontendAppConfig, form, schemeName)(fakeRequest, messages).toString

  "DeleteScheme Controller" must {

    "return OK and the correct view for a GET" in {
      when(fakeDataCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the start of journey when user answers Yes" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.WhatYouWillNeedController.onPageLoad().url)
    }

    "redirect to the overview page when user answers No" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller().onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SchemesOverviewController.onPageLoad().url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
