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

import config.FeatureSwitchManagementService
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.trustees.MoreThanTenTrusteesId
import identifiers.{IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId, _}
import models.register.Entity
import models.{Link, Mode, UpdateMode}
import play.api.i18n.Messages
import utils.{Toggles, UserAnswers}
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers,
                                 viewOnly: Boolean, srn: Option[String],
                                 featureSwitchManagementService: FeatureSwitchManagementService
                                )(implicit messages: Messages) extends HsTaskListHelper(answers) {

  override protected lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__scheme_info_link_text")

  override def declarationEnabled(userAnswers: UserAnswers): Boolean = {
    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      Some(userAnswers.allEstablishersCompleted),
      Some(isTrusteeOptional | userAnswers.isAllTrusteesCompleted),
      Some(userAnswers.allTrusteesAfterDelete.size < 10 || userAnswers.get(MoreThanTenTrusteesId).isDefined)
    ).forall(_.contains(true)) && userAnswers.isUserAnswerUpdated
  }

  def taskList: SchemeDetailsTaskList = {
    val schemeName = answers.get(SchemeNameId).getOrElse("")
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      messages("messages__schemeTaskList__about_scheme_header", schemeName),
      aboutSection(answers),
      None,
      addEstablisherHeader(answers, UpdateMode, srn),
      establishers(answers),
      addTrusteeHeader(answers, UpdateMode, srn),
      trustees(answers),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      Some(messages("messages__schemeTaskList__scheme_information_link_text")),
      messages("messages__scheme_details__title"),
      srn
    )

  }

  override protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.get(IsAboutMembersCompleteId) match {
      case Some(true) => Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url)
      case _ => Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = Link(aboutBenefitsAndInsuranceLinkText,
      controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url)

    Seq(SchemeDetailsTaskListSection(None, membersLink, None),
      SchemeDetailsTaskListSection(None, benefitsAndInsuranceLink, None))
  }

  private def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    SchemeDetailsTaskListSection(
      None,
      beforeYouStartLink(answers, UpdateMode, srn),
      None
    )
  }

  protected[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] =
    if (viewOnly) {
      None
    } else {
      Some(SchemeDetailsTaskListDeclarationSection(
        header = "messages__schemeTaskList__sectionDeclaration_header",
        declarationLink = variationDeclarationLink(userAnswers, srn),
        incompleteDeclarationText =
          "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))
    }

  private[utils] def variationDeclarationLink(userAnswers: UserAnswers, srn: Option[String]): Option[Link] = {
    if (userAnswers.isUserAnswerUpdated) {
      Some(Link(declarationLinkText,
        if (userAnswers.areVariationChangesCompleted)
          controllers.routes.VariationDeclarationController.onPageLoad(srn).url
        else
          controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url
      ))
    } else {
      None
    }
  }

  protected[utils] def establishers(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListEntitySection] = {
    val sections = userAnswers.allEstablishers
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        section.id match {
          case CompanyDetailsId(_) if featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              Seq(
                EntityItem(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", section.name),
                  establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(UpdateMode, srn, section.index).url), None),
                EntityItem(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", section.name),
                  establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(UpdateMode, srn, section.index).url), None),
                EntityItem(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", section.name),
                  establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(UpdateMode, srn, section.index).url), None),
                EntityItem(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", section.name),
                  establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(UpdateMode, srn, section.index).url, None))
              ),
              Some(section.name))
            )

          case _ => Some(SchemeDetailsTaskListEntitySection(
            None,
            Seq(EntityItem(Link(messages("messages__schemeTaskList__persons_details__link_text", section.name),
              section.editLink(UpdateMode, srn).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)), None)),
            None)
          )
        }

      }
    }
    notDeletedElements.flatten
  }

  protected[utils] def trustees(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] =
    listOfSectionNameAsLink(userAnswers.allTrustees)

  private def listOfSectionNameAsLink(sections: Seq[Entity[_]]): Seq[SchemeDetailsTaskListSection] = {
    val notDeletedElements = for ((section, index) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        Some(SchemeDetailsTaskListSection(
          None,
          Link(messages("messages__schemeTaskList__persons_details__link_text", section.name),
            section.editLink(UpdateMode, srn).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)),
          None)
        )
      }
    }
    notDeletedElements.flatten
  }

  protected[utils] def addEstablisherHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] =
    (userAnswers.allEstablishersAfterDelete.isEmpty, viewOnly) match {
      case (true, true) => Some(SchemeDetailsTaskListHeader(plainText = Some(noEstablishersText)))
      case (true, false) => Some(SchemeDetailsTaskListHeader(link = typeOfEstablisherLink(addEstablisherLinkText, userAnswers.allEstablishers.size, srn, mode)))
      case (false, false) => Some(SchemeDetailsTaskListHeader(link = addEstablisherLink(changeEstablisherLinkText, srn, mode)))
      case (false, true) => None
    }

  protected[utils] override def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] =
    (userAnswers.allTrusteesAfterDelete.isEmpty, viewOnly) match {
      case (true, true) => Some(SchemeDetailsTaskListHeader(plainText = Some(noTrusteesText)))
      case (true, false) => Some(SchemeDetailsTaskListHeader(
        link = typeOfTrusteeLink(addTrusteesLinkText, userAnswers.allTrustees.size, srn, mode)))
      case (false, false) => {

        val (linkText, additionalText): (String, Option[String]) =
          getTrusteeHeaderText(userAnswers.allTrusteesAfterDelete.size, userAnswers.get(SchemeTypeId))

        Some(
          SchemeDetailsTaskListHeader(
            link = addTrusteeLink(linkText, srn, mode),
            p1 = additionalText))
      }
      case (false, true) => None
    }
}
