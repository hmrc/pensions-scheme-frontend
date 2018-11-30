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

import identifiers.register.IsAboutSchemeCompleteId
import identifiers.register.adviser.IsWorkingKnowledgeCompleteId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import models.NormalMode
import models.register.Entity
import viewmodels.{JourneyTaskList, JourneyTaskListSection, Link}
import play.api.i18n.Messages

class TaskListHelper(journey: UserAnswers)(implicit messages: Messages) {

  def tasklist: JourneyTaskList = JourneyTaskList(
    aboutSection,
    listOf(journey.allEstablishersAfterDelete),
    listOf(journey.allTrusteesAfterDelete),
    workingKnowledgeSection,
    declarationLink)

  private def aboutSection = JourneyTaskListSection(
    journey.get(IsAboutSchemeCompleteId),
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url),
    None)

  private def workingKnowledgeSection = JourneyTaskListSection(
    journey.get(IsWorkingKnowledgeCompleteId),
    Link(messages("messages__schemeTaskList__working_knowledge_add_link"),
      controllers.routes.WorkingKnowledgeController.onPageLoad.url),
    None)

  private def declarationLink: Option[Link] =
    if (declarationEnabled)
      Some(Link(messages("messages__schemeTaskList__declaration_link"),
        controllers.register.routes.DeclarationController.onPageLoad().url))
    else None

  private def declarationEnabled: Boolean =
    (journey.get(IsAboutSchemeCompleteId), journey.get(IsWorkingKnowledgeCompleteId)) match {
      case (Some(true), Some(true)) if journey.allEstablishersAfterDelete.forall(_.isCompleted) &&
        journey.allTrusteesAfterDelete.forall(_.isCompleted) => true
      case _ => false
    }

  private def listOf(sections: Seq[Entity[_]]): Seq[JourneyTaskListSection] =
    for(section <- sections) yield
      JourneyTaskListSection(
        Some(section.isCompleted),
        Link(linkText(section),
          section.editLink),
        Some(section.name))

  private def linkText(item: Entity[_]): String =
    item.id match {
      case EstablisherCompanyDetailsId(_) | TrusteeCompanyDetailsId(_) => messages("messages__schemeTaskList__company_link")
      case EstablisherDetailsId(_) | TrusteeDetailsId(_) => messages("messages__schemeTaskList__individual_link")
      case EstablisherPartnershipDetailsId(_) | TrusteePartnershipDetailsId(_) => messages("messages__schemeTaskList__partnership_link")
    }
}
