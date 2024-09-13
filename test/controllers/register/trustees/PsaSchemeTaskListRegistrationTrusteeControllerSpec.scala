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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions.{DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.SchemeNameId
import models.{EntitySpoke, NormalMode, TaskListLink}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import utils.UserAnswers
import utils.hstasklisthelper.HsTaskListHelperRegistration
import viewmodels.{Message, SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListTrustees, StatsSection}
import views.html.register.trustees.psaTaskListRegistrationTrustees

class PsaSchemeTaskListRegistrationTrusteeControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import PsaSchemeTaskListRegistrationTrusteeControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(mockHsTaskListHelperRegistration)
  }

  "PsaSchemeTaskListRegistrationTrustee Controller" must {
    "return OK and the correct view for a GET when scheme name is present" in {
      when(mockHsTaskListHelperRegistration.taskListTrustee(any(), any(), any(), any()))
        .thenReturn(schemeDetailsTaskListTrustees)

      val result = controller(new FakeDataRetrievalAction(Some(userAnswersWithSchemeName.json)))
        .onPageLoad(NormalMode, 0, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(schemeDetailsTaskListTrustees)
    }

    "redirect to Session Expired for a GET if srn specified" in {
      when(mockHsTaskListHelperRegistration.taskListTrustee(any(), any(), any(), any()))
        .thenReturn(schemeDetailsTaskListTrustees)

      val result = controller(new FakeDataRetrievalAction(Some(userAnswersWithSchemeName.json)))
        .onPageLoad(NormalMode, 0, Some("srn"))(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Page Not Found for a GET if RuntimeException with 'INVALID-TRUSTEE' is thrown" in {
      when(mockHsTaskListHelperRegistration.taskListTrustee(any(), any(), any(), any()))
        .thenThrow(new RuntimeException("INVALID-TRUSTEE"))

      val result = controller(new FakeDataRetrievalAction(Some(userAnswersWithSchemeName.json)))
        .onPageLoad(NormalMode, 0, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.routes.MemberNotFoundController.onTrusteesPageLoad().url)
    }
  }
}

object PsaSchemeTaskListRegistrationTrusteeControllerSpec extends PsaSchemeTaskListRegistrationTrusteeControllerSpec with MockitoSugar {
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

  private val schemeDetailsTaskListTrustees: SchemeDetailsTaskListTrustees =
    SchemeDetailsTaskListTrustees(
      h1 = h1,
      srn = srn,
      trustee = entitySection,
      allComplete = true,
      statsSection = Some(StatsSection(
        sectionsCompleted = 3,
        totalSections = 3,
        dateExpiry = None
      ))
    )

  private val mockHsTaskListHelperRegistration = mock[HsTaskListHelperRegistration]

  private val view = injector.instanceOf[psaTaskListRegistrationTrustees]

  private def controller(dataRetrievalAction: DataRetrievalAction): PsaSchemeTaskListRegistrationTrusteeController =
    new PsaSchemeTaskListRegistrationTrusteeController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      controllerComponents,
      view,
      mockHsTaskListHelperRegistration
    )


  private def viewAsString(taskSections: SchemeDetailsTaskListTrustees): String =
    view(
      taskSections,
      schemeName,
      controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url
    )(fakeRequest, messages).toString
}




