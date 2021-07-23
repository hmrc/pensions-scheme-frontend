/*
 * Copyright 2021 HM Revenue & Customs
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

import base.JsonFileReader
import connectors.{MinimalPsaConnector, SchemeDetailsConnector}
import controllers.actions._
import identifiers.racdac.IsRacDacId
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{redirectLocation, status, _}
import utils.UserAnswers

import scala.concurrent.Future

class PsaSchemeTaskListControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import PsaSchemeTaskListControllerSpec._

  override protected def beforeEach(): Unit = {
    when(mockMinimalPsaConnector.getMinimalFlags(any())(any(), any()))
      .thenReturn(Future.successful(PSAMinimalFlags(false, false, false)))
  }

  "PSASchemeTaskList Controller" must {
    "work" in {
      val userAnswers = UserAnswers().setOrException(IsRacDacId)(true)
      when(mockSchemeDetailsConnector.getSchemeDetails(any(),any(), any())(any(),any()))
        .thenReturn(Future.successful(userAnswers))
      val result = controller.onPageLoad(UpdateMode, srn)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(UpdateMode, srn).url)


    }

  }
}

object PsaSchemeTaskListControllerSpec extends ControllerSpecBase with MockitoSugar with JsonFileReader {
  private val mockMinimalPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]
  private val mockSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]

  private val srn = Some("srn")

  private def controller: PsaSchemeTaskListController =
    new PsaSchemeTaskListController(
      frontendAppConfig,
      mockSchemeDetailsConnector,
      mockMinimalPsaConnector,
      messagesApi,
      FakeAuthAction,
      controllerComponents
    )
}
