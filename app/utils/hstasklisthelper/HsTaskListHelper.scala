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

package utils.hstasklisthelper

import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.establishers.{company => establisherCompany}
import identifiers.register.trustees.MoreThanTenTrusteesId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId, _}
import models._
import models.register.SchemeType.{MasterTrust, SingleTrust}
import models.register.{Entity, SchemeType}
import play.api.i18n.Messages
import utils.{Enumerable, UserAnswers}
import viewmodels._

abstract class HsTaskListHelper(answers: UserAnswers
                               )(implicit val messages: Messages) extends Enumerable.Implicits with HsTaskListHelperUtils with AllSpokes {

  protected val schemeName: String = answers.get(SchemeNameId).getOrElse("")

  protected val beforeYouStartLinkText: String

  protected lazy val aboutMembersLinkText: String = messages("messages__schemeTaskList__about_members_link_text", schemeName)
  protected lazy val aboutMembersViewLinkText: String = messages("messages__schemeTaskList__about_members_link_text_view", schemeName)
  protected lazy val aboutMembersAddLinkText: String = messages("messages__schemeTaskList__about_members_link_text_add", schemeName)
  protected lazy val aboutBenefitsAndInsuranceLinkText: String = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text", schemeName)
  protected lazy val aboutBenefitsAndInsuranceViewLinkText: String = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName)
  protected lazy val aboutBenefitsAndInsuranceAddLinkText: String = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text_add", schemeName)
  protected lazy val aboutBankDetailsLinkText: String = messages("messages__schemeTaskList__about_bank_details_link_text", schemeName)
  protected lazy val aboutBankDetailsAddLinkText: String = messages("messages__schemeTaskList__about_bank_details_link_text_add", schemeName)
  protected lazy val workingKnowledgeLinkText: String = messages("messages__schemeTaskList__change_details", schemeName)
  protected lazy val workingKnowledgeAddLinkText: String = messages("messages__schemeTaskList__add_details")
  protected lazy val addEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  protected lazy val changeEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  protected lazy val viewEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_view_link")
  protected lazy val companyLinkText: String = messages("messages__schemeTaskList__company_link")
  protected lazy val individualLinkText: String = messages("messages__schemeTaskList__individual_link")
  protected lazy val partnershipLinkText: String = messages("messages__schemeTaskList__partnership_link")
  protected lazy val addTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_add_link")
  protected lazy val addDeleteTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val addTrusteesAdditionalInfo: String = messages("messages__schemeTaskList__sectionTrustees_add_additional_text")
  protected lazy val changeTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val viewTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_view_link")
  protected lazy val deleteTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_delete_link")
  protected lazy val deleteTrusteesAdditionalInfo: String = messages("messages__schemeTaskList__sectionTrustees_delete_additional_text")
  protected lazy val declarationLinkText: String = messages("messages__schemeTaskList__declaration_link")
  protected lazy val noEstablishersText: String = messages("messages__schemeTaskList__sectionEstablishers_no_establishers")
  protected lazy val noTrusteesText: String = messages("messages__schemeTaskList__sectionTrustees_no_trustees")

  def taskList: SchemeDetailsTaskList

  protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection]

  private[utils] def beforeYouStartLink(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Link = {
    userAnswers.get(IsBeforeYouStartCompleteId) match {
      case Some(true) => Link(beforeYouStartLinkText, controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(mode, srn).url)
      case _ => Link(beforeYouStartLinkText, controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }
  }

  private[utils] def workingKnowledgeSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListSection] = {
    userAnswers.get(DeclarationDutiesId) match {
      case Some(false) =>
        val wkLink = userAnswers.get(IsWorkingKnowledgeCompleteId) match {
          case Some(true) => Link(workingKnowledgeLinkText, controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url)
          case Some(false) => Link(workingKnowledgeLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
          case None => Link(workingKnowledgeAddLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
        }
        Some(SchemeDetailsTaskListSection(userAnswers.get(IsWorkingKnowledgeCompleteId), wkLink, None))
      case _ =>
        None
    }
  }

  protected[utils] def addEstablisherHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader]

  protected[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader]

  protected def typeOfTrusteeLink(linkText: String, trusteeCount: Int, srn: Option[String], mode: Mode): Option[Link] =
    Some(Link(linkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, trusteeCount, srn).url))

  protected def addTrusteeLink(linkText: String, srn: Option[String], mode: Mode): Option[Link] =
    Some(Link(linkText, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url))

  protected def typeOfEstablisherLink(linkText: String, establisherCount: Int, srn: Option[String], mode: Mode): Option[Link] =
    Some(Link(linkText, controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, establisherCount, srn).url))

  protected def addEstablisherLink(linkText: String, srn: Option[String], mode: Mode): Option[Link] =
    Some(Link(linkText, controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url))

  protected[utils] def trusteeStatus(completed: Boolean, mandatory: Boolean): Option[Boolean] = (completed, mandatory) match {
    case (true, _) => None
    case (false, false) => None
    case (false, true) => Some(false)
  }

  private[utils] def getTrusteeHeaderText(size: Int, schemeType: Option[SchemeType]): (String, Option[String]) = size match {
    case `size` if size == 10 => (deleteTrusteesLinkText, Some(deleteTrusteesAdditionalInfo))
    case `size` if size == 1 && trusteesMandatory(schemeType) => (addTrusteesLinkText, Some(addTrusteesAdditionalInfo))
    case `size` if size > 1 && size < 10 => (addDeleteTrusteesLinkText, None)
    case _ => (changeTrusteesLinkText, None)
  }

  private[utils] def trusteesMandatory(schemeType: Option[SchemeType]): Boolean = {
    schemeType.contains(MasterTrust) || schemeType.contains(SingleTrust)
  }

  private[utils] def declarationEnabled(userAnswers: UserAnswers): Boolean = {

    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBankDetailsCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      userAnswers.get(IsWorkingKnowledgeCompleteId),
      Some(isAllEstablishersCompleted(userAnswers, NormalMode)),
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
    case establisherCompany.CompanyDetailsId(_) | TrusteeCompanyDetailsId(_) => companyLinkText
    case EstablisherNameId(_) | TrusteeNameId(_) => individualLinkText
    case EstablisherPartnershipDetailsId(_) | TrusteePartnershipDetailsId(_) => partnershipLinkText
  }

  protected def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean = {
    userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)
  }

  protected def isAllEstablishersCompleted(userAnswers: UserAnswers, mode: Mode): Boolean = {
    userAnswers.allEstablishersAfterDelete(mode).nonEmpty &&
      userAnswers.allEstablishersAfterDelete(mode).forall(_.isCompleted)
  }

  protected[utils] def establishers(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Seq[SchemeDetailsTaskListEntitySection] = {
    val sections = userAnswers.allEstablishers(mode)
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        section.id match {
          case establisherCompany.CompanyDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getEstablisherCompanySpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case EstablisherNameId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getEstablisherIndividualSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case EstablisherPartnershipDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getEstablisherPartnershipSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case _ if mode == NormalMode =>
            Some(SchemeDetailsTaskListEntitySection(
              Some(section.isCompleted),
              Seq(EntitySpoke(Link(linkText(section),
                section.editLink(NormalMode, None).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)), Some(section.isCompleted))),
              Some(section.name))
            )

          case _ =>
            Some(SchemeDetailsTaskListEntitySection(
            None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__persons_details__link_text", section.name),
              section.editLink(UpdateMode, srn).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)), None)),
            None)
          )
        }
      }
    }
    notDeletedElements.flatten
  }

  protected[utils] def trustees(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Seq[SchemeDetailsTaskListEntitySection] = {
    val sections = userAnswers.allTrustees
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        section.id match {
          case TrusteeCompanyDetailsId(_) => // Trustee companies
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getTrusteeCompanySpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case TrusteeNameId(_) => // Trustee individuals
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getTrusteeIndividualSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case TrusteePartnershipDetailsId(_) => // Trustee partnership
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getTrusteePartnershipSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case _ if mode == NormalMode =>
            Some(SchemeDetailsTaskListEntitySection(
              Some(section.isCompleted),
              Seq(EntitySpoke(Link(linkText(section),
                section.editLink(NormalMode, None).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)), Some(section.isCompleted))),
              Some(section.name))
            )

          case _ => Some(SchemeDetailsTaskListEntitySection(
            None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__persons_details__link_text", section.name),
              section.editLink(UpdateMode, srn).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)), None)),
            None)
          )
        }
      }
    }
    notDeletedElements.flatten
  }
}
