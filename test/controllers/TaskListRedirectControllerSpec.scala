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

package controllers

import base.JsonFileReader
import connectors.{MinimalPsaConnector, SchemeDetailsConnector}
import controllers.actions._
import identifiers.racdac.IsRacDacId
import models._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{redirectLocation, status, _}
import services.FakeUserAnswersService.appConfig
import utils.UserAnswers

import scala.concurrent.Future

class TaskListRedirectControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import TaskListRedirectControllerSpec._

  override protected def beforeEach(): Unit = {
    when(mockMinimalPsaConnector.getMinimalFlags()(any(), any()))
      .thenReturn(Future.successful(PSAMinimalFlags(false, false, false)))
  }

  "PSASchemeTaskList Controller" must {
    "work" in {
      val userAnswers = UserAnswers().setOrException(IsRacDacId)(true)
      when(mockSchemeDetailsConnector.getSchemeDetails(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(userAnswers))
      val result = controller.onPageLoad(UpdateMode, srn)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(UpdateMode, srn).url)


    }

  }
  "PsaMinimalFlag has isDeceased flag as True and rlsFlag as false" must {
    "return REDIRECT to youMustContactHMRCUrl" in {
      val psaMinimalFlags = PSAMinimalFlags(false, true, false)
      when(mockMinimalPsaConnector.getMinimalFlags()(any(), any()))
        .thenReturn(Future.successful(psaMinimalFlags))
      val result = controller.onPageLoad(UpdateMode, srn)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(appConfig.youMustContactHMRCUrl)
    }
  }

  "PsaMinimalFlag has isDeceased flag as false and rlsFlag as true" must {
    "return REDIRECT to psaUpdateContactDetailsUrl" in {
      val psaMinimalFlags = PSAMinimalFlags(true, false, true)
      when(mockMinimalPsaConnector.getMinimalFlags()(any(), any()))
        .thenReturn(Future.successful(psaMinimalFlags))
      val result = controller.onPageLoad(UpdateMode, srn)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(appConfig.psaUpdateContactDetailsUrl)
    }
  }


}

object TaskListRedirectControllerSpec extends ControllerSpecBase with MockitoSugar with JsonFileReader {
  private val mockMinimalPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]
  private val mockSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]

  private val srn = Some(SchemeReferenceNumber("srn"))

  private def controller: TaskListRedirectController =
    new TaskListRedirectController(
      frontendAppConfig,
      mockSchemeDetailsConnector,
      mockMinimalPsaConnector,
      messagesApi,
      FakeAuthAction,
      controllerComponents
    )
}
