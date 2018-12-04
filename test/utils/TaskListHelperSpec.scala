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
import models.NormalMode
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
}

object TaskListHelperSpec extends SpecBase with JsonFileReader {

  private val aboutSectionDefaultLink: Link = {
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url)
  }

  private val workingKnowledgeDefaultLink: Link = {
    Link(messages("messages__schemeTaskList__working_knowledge_add_link"),
      controllers.routes.WorkingKnowledgeController.onPageLoad().url)
  }

  val userAnswersJson = readJsonFromFile("/payload.json")
  val userAnswers = UserAnswers(userAnswersJson)
  val expectedAboutSection = JourneyTaskListSection(
    Some(true),
    aboutSectionDefaultLink,
    None)

  val expectedWorkingKnowledgeSection = JourneyTaskListSection(
    Some(true),
    workingKnowledgeDefaultLink,
    None)

  val expectedEstablishersSection = Seq(
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__company_link"),
        controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(0).url),
      Some("Test company name")),
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__individual_link"),
        controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(1).url),
      Some("Test individual name")))

  val expectedTrusteesSection = Seq(
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__partnership_link"),
        controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(0).url),
      Some("Test partnership name")))

  val expectedDeclarationLink = Some(Link(messages("messages__schemeTaskList__declaration_link"),
    controllers.register.routes.DeclarationController.onPageLoad().url))

  private def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)
}
