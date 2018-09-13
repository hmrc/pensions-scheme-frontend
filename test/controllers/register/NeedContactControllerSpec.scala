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

package controllers.register

import connectors.{FakeDataCacheConnector, PSANameCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.NeedContactFormProvider
import identifiers.PsaEmailId
import models.NormalMode
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import views.html.register.needContact

import scala.concurrent.Future

class NeedContactControllerSpec extends ControllerSpecBase with MockitoSugar {

  private def onwardRoute = controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)

  val formProvider = new NeedContactFormProvider()
  val form = formProvider()

  val fakePsaNameCacheConnector = mock[PSANameCacheConnector]

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new NeedContactController(frontendAppConfig, messagesApi, FakeDataCacheConnector, FakeAuthAction,
      formProvider, fakePsaNameCacheConnector)

  private def viewAsString(form: Form[_] = form) = needContact(frontendAppConfig, form)(fakeRequest, messages).toString

  val testEmail = "test@test.com"

  "NeedContact Controller" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {

      "save the email and redirect to the Scheme details page when Psa Name exists" in {
        reset(fakePsaNameCacheConnector)
        when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(Future.successful(Some(Json.obj("psaName" -> "test name"))))
        when(fakePsaNameCacheConnector.save(any(), eqTo(PsaEmailId), eqTo(testEmail))(any(), any(), any())).thenReturn(Future.successful(Json.obj()))

        val result = controller().onSubmit(fakeRequest.withFormUrlEncodedBody(("email" -> testEmail)))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        verify(fakePsaNameCacheConnector, times(1)).save(any(), eqTo(PsaEmailId), eqTo(testEmail))(any(), any(), any())
      }

      "don't save the email and redirect to the Scheme details page when psa name does not exist" in {
        reset(fakePsaNameCacheConnector)
        when(fakePsaNameCacheConnector.fetch(any())(any(), any())).thenReturn(Future.successful(None))
        val result = controller().onSubmit(fakeRequest.withFormUrlEncodedBody(("email" -> testEmail)))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        verify(fakePsaNameCacheConnector, never()).save(any(), eqTo(PsaEmailId), eqTo(testEmail))(any(), any(), any())
      }
    }
  }
}
