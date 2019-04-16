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

import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.MoreThanTenTrusteesId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId, _}
import models.register.Entity
import models.{Link, Mode, NormalMode}
import play.api.i18n.Messages
import viewmodels._

abstract class HsTaskListHelper(answers: UserAnswers)(implicit messages: Messages) extends Enumerable.Implicits {

  protected val beforeYouStartLinkText: String
  protected lazy val aboutMembersLinkText = messages("messages__schemeTaskList__about_members_link_text")
  protected lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  protected lazy val aboutBankDetailsLinkText = messages("messages__schemeTaskList__about_bank_details_link_text")
  protected lazy val workingKnowledgeLinkText = messages("messages__schemeTaskList__working_knowledge_link_text")
  protected lazy val addEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  protected lazy val changeEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  protected lazy val companyLinkText = messages("messages__schemeTaskList__company_link")
  protected lazy val individualLinkText = messages("messages__schemeTaskList__individual_link")
  protected lazy val partnershipLinkText = messages("messages__schemeTaskList__partnership_link")
  protected lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")
  protected lazy val changeTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val declarationLinkText = messages("messages__schemeTaskList__declaration_link")

  def taskList: SchemeDetailsTaskList

  protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection]

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): SchemeDetailsTaskListSection = {
    val link = userAnswers.get(IsBeforeYouStartCompleteId) match {
      case Some(true) => Link(beforeYouStartLinkText, controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(mode, srn).url)
      case _ => Link(beforeYouStartLinkText, controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }
    SchemeDetailsTaskListSection(userAnswers.get(IsBeforeYouStartCompleteId), link, None)
  }

  private[utils] def workingKnowledgeSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListSection] = {
    userAnswers.get(DeclarationDutiesId) match {
      case Some(false) =>
        val wkLink = userAnswers.get(IsWorkingKnowledgeCompleteId) match {
          case Some(true) => Link(workingKnowledgeLinkText, controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url)
          case _ => Link(workingKnowledgeLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
        }
        Some(SchemeDetailsTaskListSection(userAnswers.get(IsWorkingKnowledgeCompleteId), wkLink, None))
      case _ =>
        None
    }
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): SchemeDetailsTaskListSection = {
    if (userAnswers.allEstablishersAfterDelete.isEmpty) {
      SchemeDetailsTaskListSection(None, Link(addEstablisherLinkText,
        controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode,
          userAnswers.allEstablishers.size, srn).url), None)
    } else {
      SchemeDetailsTaskListSection(None, Link(changeEstablisherLinkText,
        controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url), None)
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListSection] = {
    userAnswers.get(HaveAnyTrusteesId) match {
      case None | Some(true) =>
        if (userAnswers.allTrusteesAfterDelete.nonEmpty) {
          Some(
            SchemeDetailsTaskListSection(
              Some(isAllTrusteesCompleted(userAnswers)),
              Link(changeTrusteesLinkText,
                controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url),
              None
            )
          )
        } else {
          Some(
            SchemeDetailsTaskListSection(None,
              Link(addTrusteesLinkText,
                controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url),
              None
            )
          )
        }
      case _ =>
        None
    }
  }


  private[utils] def declarationEnabled(userAnswers: UserAnswers): Boolean = {
    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBankDetailsCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      userAnswers.get(IsWorkingKnowledgeCompleteId),
      Some(isAllEstablishersCompleted(userAnswers)),
      Some(isTrusteeOptional | isAllTrusteesCompleted(userAnswers)),
      Some(userAnswers.allTrusteesAfterDelete.size < 10 || userAnswers.get(MoreThanTenTrusteesId).isDefined)
    ).forall(_.contains(true))
  }

  private[utils] def declarationLink(userAnswers: UserAnswers): Option[Link] = {
    if (declarationEnabled(userAnswers))
      Some(Link(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url))
    else None
  }

  protected[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection]

  protected def linkText(item: Entity[_]): String = item.id match {
    case EstablisherCompanyDetailsId(_) | TrusteeCompanyDetailsId(_) => companyLinkText
    case EstablisherDetailsId(_) | TrusteeDetailsId(_) => individualLinkText
    case EstablisherPartnershipDetailsId(_) | TrusteePartnershipDetailsId(_) => partnershipLinkText
  }

    protected def linkTarget(item: Entity[_], index: Int, mode: Mode, srn: Option[String]): String = {
    item match {
      case models.register.EstablisherCompanyEntity(_, _, _, true, _) =>
        controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, srn, index).url
      case models.register.EstablisherPartnershipEntity(_, _, _, true, _) =>
        controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(mode, index, srn).url
      case _ => item.editLink(mode, srn).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }


  protected def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean = {
    userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)
  }

  protected def isAllEstablishersCompleted(userAnswers: UserAnswers): Boolean = {
    userAnswers.allEstablishersAfterDelete.nonEmpty && userAnswers.allEstablishersAfterDelete.forall(_.isCompleted)
  }
}
