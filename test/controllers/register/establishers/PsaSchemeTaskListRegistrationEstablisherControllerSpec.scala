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

package controllers.register.establishers

import controllers.ControllerSpecBase
import controllers.actions.{DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.SchemeNameId
import models.{EntitySpoke, NormalMode, SchemeReferenceNumber, TaskListLink}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import utils.UserAnswers
import utils.hstasklisthelper.HsTaskListHelperRegistration
import viewmodels.{Message, SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListEstablishers, StatsSection}
import views.html.register.establishers.psaTaskListRegistrationEstablishers

class PsaSchemeTaskListRegistrationEstablisherControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import PsaSchemeTaskListRegistrationEstablisherControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(mockHsTaskListHelperRegistration)
  }

  "PsaSchemeTaskListRegistrationEstablisher Controller" must {
    "return OK and the correct view for a GET when scheme name is present" in {
      when(mockHsTaskListHelperRegistration.taskListEstablisher(any(), any(), any(), any()))
        .thenReturn(schemeDetailsTaskListEstablishers)

      val result = controller(new FakeDataRetrievalAction(Some(userAnswersWithSchemeName.json)))
        .onPageLoad(NormalMode, 0, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(schemeDetailsTaskListEstablishers)
    }

    "redirect to Session Expired for a GET if srn specified" in {
      when(mockHsTaskListHelperRegistration.taskListEstablisher(any(), any(), any(), any()))
        .thenReturn(schemeDetailsTaskListEstablishers)

      val result = controller(new FakeDataRetrievalAction(Some(userAnswersWithSchemeName.json)))
        .onPageLoad(NormalMode, 0, Some(SchemeReferenceNumber("srn")))(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}

object PsaSchemeTaskListRegistrationEstablisherControllerSpec extends PsaSchemeTaskListRegistrationEstablisherControllerSpec with MockitoSugar {
  private val srn = None
  private val h1 = "h1"
  private val schemeName = "scheme"
  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value
  private val target = "/target"
  private val entitySection: SchemeDetailsTaskListEntitySection = SchemeDetailsTaskListEntitySection(
    isCompleted = Some(true),
    entities = Seq(
      EntitySpoke(TaskListLink(
        Message("test-msg"),
        target
      ), None)
    ),
    header = None,
    p1 = ""
  )

  private val schemeDetailsTaskListEstablishers: SchemeDetailsTaskListEstablishers =
    SchemeDetailsTaskListEstablishers(
      h1 = h1,
      srn = srn,
      establisher = entitySection,
      allComplete = true,
      statsSection = Some(StatsSection(
        sectionsCompleted = 3,
        totalSections = 3,
        dateExpiry = None
      ))
    )

  private val mockHsTaskListHelperRegistration = mock[HsTaskListHelperRegistration]

  private val view = injector.instanceOf[psaTaskListRegistrationEstablishers]

  private def controller(dataRetrievalAction: DataRetrievalAction): PsaSchemeTaskListRegistrationEstablisherController =
    new PsaSchemeTaskListRegistrationEstablisherController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      controllerComponents,
      view,
      mockHsTaskListHelperRegistration
    )


  private def viewAsString(taskSections: SchemeDetailsTaskListEstablishers): String =
    view(
      taskSections,
      schemeName,
      controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url
    )(fakeRequest, messages).toString
}


