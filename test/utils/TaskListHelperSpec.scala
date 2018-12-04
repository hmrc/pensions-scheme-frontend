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

package utils

import base.{JsonFileReader, SpecBase}
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.{CheckMode, Index, NormalMode}
import models.register.{EstablisherCompanyEntity, EstablisherIndividualEntity, EstablisherPartnershipEntity}
import org.scalatest.{MustMatchers, WordSpec}
import viewmodels._

class TaskListHelperSpec extends WordSpec with MustMatchers {

  import TaskListHelperSpec._

  "TaskListHelper" must {
    "return valid about section based on user answers" in {
      new TaskListHelper(Some(userAnswers)).tasklist mustBe JourneyTaskList(expectedAboutSection, expectedEstablishersSection,
        expectedTrusteesSection, expectedWorkingKnowledgeSection, expectedDeclarationLink)
    }

    "return blank task list if there are no user answers" in {
      val blankJourneyTaskList = JourneyTaskList(
        JourneyTaskListSection(None, aboutSectionDefaultLink, None),
        Seq.empty,
        Seq.empty,
        JourneyTaskListSection(None, workingKnowledgeDefaultLink, None),
        None)

      new TaskListHelper(None).tasklist mustBe blankJourneyTaskList
    }
  }

  "linkTarget" must {

    "return correct link for establishers company if its completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherCompany, 0) mustBe
        controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0).url

    }

    "return correct link for establishers if its not completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherCompany.copy(isCompleted = false), 0) mustBe
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0).url

    }

    "return correct link for establishers partnership if its completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherPartnership, 0) mustBe
        controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(0).url

    }

    "return correct link for establishers partnership if its not completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherPartnership.copy(isCompleted = false), 0) mustBe
        controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 0).url

    }

    "return correct link for establishers individual if its completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherIndividual, 0) mustBe
        controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(0).url

    }

    "return correct link for establishers individual if its not completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherIndividual.copy(isCompleted = false), 0) mustBe
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0).url

    }
  }
}

object TaskListHelperSpec extends SpecBase with JsonFileReader {

  private val aboutSectionDefaultLink: Link = {
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url)
  }

  private val aboutSectionCompletedLink: Link = {
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.CheckYourAnswersController.onPageLoad.url)
  }

  private val workingKnowledgeDefaultLink: Link = {
    Link(messages("messages__schemeTaskList__working_knowledge_add_link"),
      controllers.routes.WorkingKnowledgeController.onPageLoad().url)
  }

  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val userAnswers = UserAnswers(userAnswersJson)
  private val expectedAboutSection = JourneyTaskListSection(
    Some(true),
    aboutSectionCompletedLink,
    None)

  private val expectedWorkingKnowledgeSection = JourneyTaskListSection(
    Some(true),
    workingKnowledgeDefaultLink,
    None)

  private val expectedEstablishersSection = Seq(
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__company_link"),
        controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0).url),
      Some("Test company name")),
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__individual_link"),
        controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(1).url),
      Some("Test individual name")))

  private val expectedTrusteesSection = Seq(
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__partnership_link"),
        controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(0).url),
      Some("Test partnership name")))

  private val expectedDeclarationLink = Some(Link(messages("messages__schemeTaskList__declaration_link"),
    controllers.register.routes.DeclarationController.onPageLoad().url))

  private def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)

  private val establisherCompany = EstablisherCompanyEntity(CompanyDetailsId(Index(0)), "Test Comapny", isDeleted = false, isCompleted = true)
  private val establisherPartnership = EstablisherPartnershipEntity(PartnershipDetailsId(Index(0)), "Test Partnership", isDeleted = false, isCompleted = true)
  private val establisherIndividual = EstablisherIndividualEntity(EstablisherDetailsId(Index(0)), "Test Partnership", isDeleted = false, isCompleted = true)
}
