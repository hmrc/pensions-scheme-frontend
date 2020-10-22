/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions._
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.hstasklisthelper.HsTaskListHelperPsp
import viewmodels._
import views.html.pspTaskList

class PspSchemeTaskListControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import PspSchemeTaskListControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(fakeHsTaskListHelper)
  }

  "PspSchemeTaskList Controller" must {

    "return OK and the correct view when there are user answers" in {
      when(fakeHsTaskListHelper.taskList(any(), any())).thenReturn(schemeDetailsTL)

      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result).contains(messages("messages__scheme_details__title")) mustBe true
      contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe false
    }
  }

  "return REDIRECT to session expired page when there are no user answers" in {
    val result = controller(new FakePspDataRetrievalAction(None)).onPageLoad(srn)(fakeRequest)

    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
  }
}

object PspSchemeTaskListControllerSpec extends ControllerSpecBase with MockitoSugar with JsonFileReader {
  private val view = injector.instanceOf[pspTaskList]
  private val fakeHsTaskListHelper = mock[HsTaskListHelperPsp]

  private val srn = "S1000000456"
  private val schemeName = "test scheme"

  def controller(dataRetrievalAction: PspDataRetrievalAction = userAnswers): PspSchemeTaskListController =
    new PspSchemeTaskListController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      stubMessagesControllerComponents(),
      view,
      fakeHsTaskListHelper
    )

  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val userAnswers = new FakePspDataRetrievalAction(Some(userAnswersJson))

  private val beforeYouStartSpoke: SchemeDetailsTaskListEntitySection = SchemeDetailsTaskListEntitySection(None,
    Seq(EntitySpoke(TaskListLink(
      Message("messages__schemeTaskList__scheme_info_link_text", schemeName),
      controllers.routes.CheckYourAnswersBeforeYouStartController.pspOnPageLoad(srn).url
    ), None)),
    Some(Message("messages__schemeTaskList__scheme_information_link_text"))
  )

  private val aboutSpoke: SchemeDetailsTaskListEntitySection = SchemeDetailsTaskListEntitySection(None,
    Seq(EntitySpoke(TaskListLink(
      Message("messages__schemeTaskList__about_members_link_psp"),
      controllers.routes.CheckYourAnswersMembersController.pspOnPageLoad(srn).url
    ), None),
      EntitySpoke(TaskListLink(
        Message("messages__schemeTaskList__about_benefits_and_insurance_link_psp"),
        controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.pspOnPageLoad(srn).url
      ), None)),
    Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
  )

  private val schemeDetailsTL = PspTaskList(schemeName, srn, beforeYouStartSpoke, aboutSpoke, Nil, None, Nil)
}
