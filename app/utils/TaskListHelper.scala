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

import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.{IsAboutSchemeCompleteId, IsWorkingKnowledgeCompleteId, SchemeDetailsId}
import models.NormalMode
import models.register.{Entity, SchemeType}
import play.api.i18n.Messages
import viewmodels.{JourneyTaskList, JourneyTaskListSection, Link}

class TaskListHelper(journey: Option[UserAnswers])(implicit messages: Messages) {

  def taskList: JourneyTaskList = {
    journey.fold(
      blankJourneyTaskList
    )(implicit userAnswers =>
      JourneyTaskList(
        aboutSection,
        listOf(userAnswers.allEstablishersAfterDelete),
        listOf(userAnswers.allTrusteesAfterDelete),
        workingKnowledgeSection,
        declarationLink,
        addTrusteeHeader)
    )
  }

  private def blankJourneyTaskList: JourneyTaskList = {
    JourneyTaskList(
      JourneyTaskListSection(None, aboutSectionDefaultLink, None),
      Seq.empty,
      Seq.empty,
      JourneyTaskListSection(None, workingKnowledgeDefaultLink, None),
      None,
      addTrusteesDefaultLink)
  }

  private lazy val aboutLinkText = messages("messages__schemeTaskList__about_link_text")
  private lazy val workingKnowledgeLinkText = messages("messages__schemeTaskList__working_knowledge_add_link")
  private lazy val changeWorkingKnowledgeLinkText = messages("messages__schemeTaskList__working_knowledge_change_link")
  private lazy val declarationLinkText = messages("messages__schemeTaskList__declaration_link")
  private lazy val companyLinkText = messages("messages__schemeTaskList__company_link")
  private lazy val individualLinkText = messages("messages__schemeTaskList__individual_link")
  private lazy val partnershipLinkText = messages("messages__schemeTaskList__partnership_link")
  private lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")
  private lazy val changeTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_change_link")

  private val aboutSectionDefaultLink: Link = Link(aboutLinkText,
    controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url)

  private val workingKnowledgeDefaultLink: Link = Link(workingKnowledgeLinkText,
    controllers.routes.WorkingKnowledgeController.onPageLoad(NormalMode).url)

  private val addTrusteesDefaultLink: JourneyTaskListSection = JourneyTaskListSection(
    None,
    Link(addTrusteesLinkText, controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url),
    None
  )


  private def aboutSection(implicit userAnswers: UserAnswers): JourneyTaskListSection = {
    val link = userAnswers.get(IsAboutSchemeCompleteId) match {
      case Some(true) => Link(aboutLinkText, controllers.register.routes.CheckYourAnswersController.onPageLoad().url)
      case _ => aboutSectionDefaultLink
    }

    JourneyTaskListSection(userAnswers.get(IsAboutSchemeCompleteId), link, None)
  }

  private def workingKnowledgeSection(implicit userAnswers: UserAnswers) = {
    val isComplete = userAnswers.get(IsWorkingKnowledgeCompleteId)
    val link = if (isComplete.getOrElse(false)) {
      Link(changeWorkingKnowledgeLinkText, controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad().url)
    } else {
      Link(workingKnowledgeLinkText, controllers.routes.WorkingKnowledgeController.onPageLoad(NormalMode).url)
    }
    JourneyTaskListSection(
      isComplete,
      link,
      None
    )
  }

  private def declarationLink(implicit userAnswers: UserAnswers): Option[Link] = {
    if (declarationEnabled)
      Some(Link(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url))
    else None
  }

  private def listOf(sections: Seq[Entity[_]]): Seq[JourneyTaskListSection] = {
    for ((section, index) <- sections.zipWithIndex) yield
      JourneyTaskListSection(
        Some(section.isCompleted),
        Link(linkText(section), linkTarget(section, index)),
        Some(section.name)
      )
  }

  private def linkText(item: Entity[_]): String = item.id match {
    case EstablisherCompanyDetailsId(_) | TrusteeCompanyDetailsId(_) => companyLinkText
    case EstablisherDetailsId(_) | TrusteeDetailsId(_) => individualLinkText
    case EstablisherPartnershipDetailsId(_) | TrusteePartnershipDetailsId(_) => partnershipLinkText
  }


  private def isAllEstablishersCompleted(implicit userAnswers: UserAnswers) : Boolean =
    userAnswers.allEstablishersAfterDelete.nonEmpty && userAnswers.allEstablishersAfterDelete.forall(_.isCompleted)

  private def isTrusteesOptional(implicit userAnswers: UserAnswers): Boolean = {
    val listOfSchemeTypeTrusts: Seq[SchemeType] = Seq(SchemeType.SingleTrust, SchemeType.MasterTrust)
    userAnswers.get(SchemeDetailsId).forall(scheme => !listOfSchemeTypeTrusts.contains(scheme.schemeType))
  }

  private def isAllTrusteesCompleted(implicit userAnswers: UserAnswers) : Boolean = {

    val isOptionalTrusteesJourney = userAnswers.get(HaveAnyTrusteesId).fold(false)(_==false) && isTrusteesOptional
    val isMandatoryTrusteesJourney = userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)

    isOptionalTrusteesJourney || isMandatoryTrusteesJourney
  }

  private[utils] def declarationEnabled(implicit userAnswers: UserAnswers): Boolean = Seq(
    userAnswers.get(IsAboutSchemeCompleteId),
    userAnswers.get(IsWorkingKnowledgeCompleteId),
    Some(isAllEstablishersCompleted),
    Some(isAllTrusteesCompleted)
  ).forall(_.contains(true))

  private[utils] def linkTarget(item: Entity[_], index : Int): String = item.id match {
    case EstablisherCompanyDetailsId(_) if item.isCompleted =>
      controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(index).url
    case EstablisherPartnershipDetailsId(_) if item.isCompleted =>
      controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(index).url
    case _ => item.editLink
  }

  private[utils] def addTrusteeHeader(implicit userAnswers: UserAnswers): JourneyTaskListSection = {

    val link = (isTrusteesOptional, userAnswers.allTrusteesAfterDelete.nonEmpty) match {
      case (false, true) =>
        Link(changeTrusteesLinkText, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode).url)
      case (false, false) =>
        Link(addTrusteesLinkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size).url)
      case (true, true) =>
        Link(changeTrusteesLinkText, controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url)
      case (true, false) if isAllTrusteesCompleted =>
        Link(changeTrusteesLinkText, controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url)
      case (true, false) =>
        Link(addTrusteesLinkText, controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode).url)
    }

    if(userAnswers.allTrusteesAfterDelete.isEmpty && isAllTrusteesCompleted)
      JourneyTaskListSection(Some(isAllTrusteesCompleted), link, None)
    else
      JourneyTaskListSection(None, link, None)

  }
}
