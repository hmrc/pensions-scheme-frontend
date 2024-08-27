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

package controllers.register

import connectors.UpdateSchemeCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.SchemeNameId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results._
import play.api.test.Helpers._
import views.html.register.schemeVariationsSuccess

import scala.concurrent.Future

class SchemeVariationsSuccessControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val fakeUserAnswersCacheConnector = mock[UpdateSchemeCacheConnector]

  val schemeName = "scheme"

  val validData: JsObject = Json.obj(
    SchemeNameId.toString -> schemeName
  )
  private val view = injector.instanceOf[schemeVariationsSuccess]

  private def controller(dataRetrievalAction: DataRetrievalAction =
                         new FakeDataRetrievalAction(Some(validData))): SchemeVariationsSuccessController =
    new SchemeVariationsSuccessController(
      frontendAppConfig,
      messagesApi,
      fakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      controllerComponents,
      view
    )

  def viewAsString(): String =
    view(
      Some(schemeName),
      (srn)
    )(fakeRequest, messages).toString

  appRunning()

  "SchemeVariationsSuccess Controller" must {
    "return OK and the correct view for a GET" in {
      when(fakeUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))

      val result = controller().onPageLoad(srn)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(fakeUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())
    }
  }

}
