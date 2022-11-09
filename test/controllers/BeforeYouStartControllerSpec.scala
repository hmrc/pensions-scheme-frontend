/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.PensionAdministratorConnector
import controllers.actions._
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import views.html.beforeYouStart

import scala.concurrent.Future

class BeforeYouStartControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach with ScalaFutures {
  val pensionAdministratorConnector: PensionAdministratorConnector = mock[PensionAdministratorConnector]
  def onwardRoute: Call = controllers.routes.SchemeNameController.onPageLoad(NormalMode)

  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]
  private val psaName = "Psa Name"
  private val view = injector.instanceOf[beforeYouStart]

  def controller(dataRetrievalAction: DataRetrievalAction = dontGetAnyData): BeforeYouStartController =
    new BeforeYouStartController(
      messagesApi,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      pensionAdministratorConnector,
      controllerComponents,
      view
    )

  val encryptedPsaId: String = applicationCrypto.QueryParameterCrypto.encrypt(PlainText("A0000000")).value

  def viewAsString(): String = view(psaName)(fakeRequest, messages).toString

  "BeforeYouStart Controller" when {

    "on a GET" must {
      "return OK and the correct view" in {

        when(pensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
        val result = controller().onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}
