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

package hscontrollers.beforeYouStart

import controllers.ControllerSpecBase
import controllers.actions._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import views.html.hs.beforeYouStart.beforeYouStart

class BeforeYouStartControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): BeforeYouStartController =
    new BeforeYouStartController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      applicationCrypto
    )

  def viewAsString(): String = beforeYouStart(frontendAppConfig)(fakeRequest, messages).toString

  "BeforeYouStart Controller" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}
