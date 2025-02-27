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

package utils.hstasklisthelper

import base.{JsonFileReader, SpecBase}
import helpers.DataCompletionHelper
import models._
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import utils.UserAnswers
import viewmodels.{Message, PspTaskList, SchemeDetailsTaskListEntitySection}

class HsTaskListHelperPspSpec extends AnyWordSpec with Matchers with MockitoSugar {

  import HsTaskListHelperPspSpec._

  private val helper = new HsTaskListHelperPsp

  "h1" must {
    "have the name of the scheme" in {

      helper.taskList(userAnswersJson, (srn)).h1 mustBe schemeName
    }
  }

  "beforeYouStartSection " must {
    "return the correct entity section " in {
      helper.taskList(userAnswersJson, (srn)).beforeYouStart mustBe beforeYouStartSpoke
    }
  }

  "aboutSection " must {
    "return the correct entity section " in {
      helper.taskList(userAnswersJson, (srn)).about mustBe aboutSpoke
    }
  }

  "addTrusteeHeader " must {
    "have no link when no trustees are added and viewOnly is true" in {
      helper.taskList(userAnswersJson, (srn)).trusteeHeader mustBe Some(SchemeDetailsTaskListEntitySection(None, Nil, None))
    }
  }

  "establishersSection" must {
    "return the seq of establishers without the deleted ones" in {
      val userAnswers = userAnswersJson.establisherCompanyEntity(index = 0).
        establisherCompanyEntity(index = 1, isDeleted = true).
        establisherIndividualEntity(index = 2).
        establisherIndividualEntity(index = 3, isDeleted = true).
        establisherPartnershipEntity(index = 4, isDeleted = true).
        establisherPartnershipEntity(index = 5)

      val result = helper.taskList(userAnswers, srn).establishers

      result mustBe Seq("test company 0", "test company 1", "first 2 last 2", "first 3 last 3", "test partnership 4", "test partnership 5")
    }
  }

  "trusteesSection" must {
    "return the seq of trustees without the deleted ones" in {
      val userAnswers = userAnswersJson.trusteeCompanyEntity(index = 0, isDeleted = true).
        trusteeCompanyEntity(index = 1).
        trusteeIndividualEntity(index = 2).
        trusteeIndividualEntity(index = 3).
        trusteePartnershipEntity(index = 4).
        trusteePartnershipEntity(index = 5)

      val result = helper.taskList(userAnswers, (srn)).trustees

      result mustBe Seq("test company 0", "test company 1", "first 2 last 2", "first 3 last 3", "test partnership 4", "test partnership 5")
    }
  }

  "task list" must {
    "return the task list with all the sections" in {

      val result = helper.taskList(userAnswersJson, (srn))

      result mustBe PspTaskList(
        schemeName, (srn),
        beforeYouStartSpoke,
        aboutSpoke,
        Seq("Test Company", "Test Individual", "Test Partnership"),
        Some(SchemeDetailsTaskListEntitySection(None, Nil, None)),
        Seq("test company", "firstName lastName", "test partnership")
      )
    }
  }
}

object HsTaskListHelperPspSpec extends SpecBase with Matchers with OptionValues with DataCompletionHelper with JsonFileReader {
  private val schemeName = "Test Scheme Name"
  private val srn = SchemeReferenceNumber("test-srn")

  private val userAnswersJson: UserAnswers = UserAnswers(readJsonFromFile("/payload.json"))

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
}
