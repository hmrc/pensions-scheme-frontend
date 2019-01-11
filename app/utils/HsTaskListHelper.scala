/*
 * Copyright 2019 HM Revenue & Customs
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

import identifiers._
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId}
import models.NormalMode
import models.register.Entity
import play.api.i18n.Messages
import viewmodels._

class HsTaskListHelper(answers: UserAnswers)(implicit messages: Messages) extends Enumerable.Implicits {

  private lazy val beforeYouStartLinkText = messages("messages__schemeDetailsTaskList__before_you_start_link_text")
  private lazy val aboutMembersLinkText = messages("messages__schemeDetailsTaskList__about_members_link_text")
  private lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeDetailsTaskList__about_benefits_and_insurance_link_text")
  private lazy val aboutBankDetailsLinkText = messages("messages__schemeDetailsTaskList__about_bank_details_link_text")
  private lazy val workingKnowledgeLinkText = messages("messages__schemeDetailsTaskList__working_knowledge_link_text")
  private lazy val addEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  private lazy val changeEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  private lazy val companyLinkText = messages("messages__schemeTaskList__company_link")
  private lazy val individualLinkText = messages("messages__schemeTaskList__individual_link")
  private lazy val partnershipLinkText = messages("messages__schemeTaskList__partnership_link")
  private lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")
  private lazy val changeTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_change_link")
  private lazy val declarationLinkText = messages("messages__schemeTaskList__declaration_link")

  def taskList: SchemeDetailsTaskList = {
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      aboutSection(answers),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers),
      establishers(answers),
      addTrusteeHeader(answers),
      trustees(answers),
      declarationLink(answers)
    )
  }

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    val link = userAnswers.get(IsBeforeYouStartCompleteId) match {
      case Some(true) => Link(beforeYouStartLinkText, controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad().url)
      case _ => Link(beforeYouStartLinkText, controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }
    SchemeDetailsTaskListSection(userAnswers.get(IsBeforeYouStartCompleteId), link, None)
  }

  private[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.get(IsAboutMembersCompleteId) match {
      case Some(true) => Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad().url)
      case _ => Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url)
    }

    val benefitsAndInsuranceLink = userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId) match {
      case Some(true) => Link(aboutBenefitsAndInsuranceLinkText, controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad().url)
      case _ => Link(aboutBenefitsAndInsuranceLinkText, controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad.url)
    }

    val bankDetailsLink = userAnswers.get(IsAboutBankDetailsCompleteId) match {
      case Some(true) => Link(aboutBankDetailsLinkText, controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad().url)
      case _ => Link(aboutBankDetailsLinkText, controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad.url)
    }

    Seq(SchemeDetailsTaskListSection(userAnswers.get(IsAboutMembersCompleteId), membersLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId), benefitsAndInsuranceLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBankDetailsCompleteId), bankDetailsLink, None))
  }

  private[utils] def workingKnowledgeSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListSection] = {
    userAnswers.get(DeclarationDutiesId) match {
      case Some(false) =>
        val wkLink = userAnswers.get(IsWorkingKnowledgeCompleteId) match {
          case Some(true) => Link(workingKnowledgeLinkText, controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad().url)
          case _ => Link(workingKnowledgeLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad.url)
        }
        Some(SchemeDetailsTaskListSection(userAnswers.get(IsWorkingKnowledgeCompleteId), wkLink, None))
      case _ =>
        None
    }
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    if (userAnswers.allEstablishersAfterDelete.isEmpty) {
      SchemeDetailsTaskListSection(None, Link(addEstablisherLinkText,
        controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, userAnswers.allEstablishers.size).url), None)
    } else {
      SchemeDetailsTaskListSection(None, Link(changeEstablisherLinkText,
        controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url), None)
    }
  }

  private[utils] def establishers(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] =
    listOf(userAnswers.allEstablishers, userAnswers)

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers): Option[SchemeDetailsTaskListSection] = {
    userAnswers.get(HaveAnyTrusteesId) match {
      case None | Some(true) =>
        if (userAnswers.allTrusteesAfterDelete.nonEmpty) {
          Some(
            SchemeDetailsTaskListSection(
              Some(isAllTrusteesCompleted(userAnswers)),
              Link(changeTrusteesLinkText, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode).url),
              None
            )
          )
        } else {
          Some(
            SchemeDetailsTaskListSection(None,
              Link(addTrusteesLinkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size).url),
              None
            )
          )
        }
      case _ =>
        None
    }
  }

  private[utils] def trustees(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] =
    listOf(userAnswers.allTrustees, userAnswers)

  private[utils] def declarationEnabled(userAnswers: UserAnswers): Boolean = {
    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBankDetailsCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      userAnswers.get(IsWorkingKnowledgeCompleteId),
      Some(isAllEstablishersCompleted(userAnswers)),
      Some(isTrusteeOptional | isAllTrusteesCompleted(userAnswers))
    ).forall(_.contains(true))
  }

  private def linkText(item: Entity[_]): String = item.id match {
    case EstablisherCompanyDetailsId(_) | TrusteeCompanyDetailsId(_) => companyLinkText
    case EstablisherDetailsId(_) | TrusteeDetailsId(_) => individualLinkText
    case EstablisherPartnershipDetailsId(_) | TrusteePartnershipDetailsId(_) => partnershipLinkText
  }

  private def linkTarget(item: Entity[_], index: Int, userAnswers: UserAnswers) = {
    item match {
      case models.register.EstablisherCompanyEntity(_, _, _, true) =>
        controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(index).url
      case models.register.EstablisherPartnershipEntity(_, _, _, true) =>
        controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(index).url
      case _ => item.editLink
    }
  }

  private def listOf(sections: Seq[Entity[_]], userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val notDeletedElements = for ((section, index) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        Some(SchemeDetailsTaskListSection(
          Some(section.isCompleted),
          Link(linkText(section), linkTarget(section, index, userAnswers)),
          Some(section.name))
        )
      }
    }
    notDeletedElements.flatten
  }

  private def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean = {
    userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)
  }

  private def isAllEstablishersCompleted(userAnswers: UserAnswers): Boolean = {
    userAnswers.allEstablishersAfterDelete.nonEmpty && userAnswers.allEstablishersAfterDelete.forall(_.isCompleted)
  }

  private def declarationLink(userAnswers: UserAnswers): Option[Link] = {
    if (declarationEnabled(userAnswers))
      Some(Link(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url))
    else None
  }

}
