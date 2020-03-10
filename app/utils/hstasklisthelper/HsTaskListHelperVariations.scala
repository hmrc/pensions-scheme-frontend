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

package utils.hstasklisthelper

import identifiers.SchemeNameId
import models.{Mode, NormalMode, TaskListLink, UpdateMode}
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers,
                                 viewOnly: Boolean,
                                 srn: Option[String]
                                ) extends HsTaskListHelper(answers) {
  import HsTaskListHelperVariations._

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    SchemeDetailsTaskListSection(
      isCompleted = None,
      link = TaskListLink(
        Message("messages__schemeTaskList__scheme_info_link_text"),
        if (answers.isBeforeYouStartCompleted(UpdateMode)) {
          controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(UpdateMode, srn).url
        } else {
          controllers.routes.SchemeNameController.onPageLoad(NormalMode).url
        }
      ),
      header = None
    )
  }

  private[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.isMembersCompleted match {
      case Some(true) => TaskListLink(aboutMembersViewLinkText(schemeName),
        controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url)
      case _ => TaskListLink(aboutMembersViewLinkText(schemeName),
        controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = TaskListLink(aboutBenefitsAndInsuranceViewLinkText(schemeName),
      controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url)

    Seq(SchemeDetailsTaskListSection(None, membersLink, None),
      SchemeDetailsTaskListSection(None, benefitsAndInsuranceLink, None))
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    (userAnswers.allEstablishersAfterDelete(mode).isEmpty, viewOnly) match {
      case (true, true) => Some(SchemeDetailsTaskListHeader(plainText = Some(noEstablishersText)))
      case (true, false) =>
        Some(
          SchemeDetailsTaskListHeader(
            link = typeOfEstablisherLink(addEstablisherLinkText, userAnswers.allEstablishers(mode).size, srn, mode)))
      case (false, false) => Some(SchemeDetailsTaskListHeader(link = addEstablisherLink(viewEstablisherLinkText, srn, mode)))
      case (false, true)  => None
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    (userAnswers.allTrusteesAfterDelete.isEmpty, viewOnly) match {
      case (true, true) => Some(SchemeDetailsTaskListHeader(plainText = Some(noTrusteesText)))
      case (true, false) => Some(SchemeDetailsTaskListHeader(
        link = typeOfTrusteeLink(addTrusteesLinkText, userAnswers.allTrustees.size, srn, mode)))
      case (false, false) => {
        Some(
          SchemeDetailsTaskListHeader(None, Some(TaskListLink(viewTrusteesLinkText,
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None))
      }
      case (false, true) => Some(SchemeDetailsTaskListHeader(header = Some(Message("messages__schemeTaskList__sectionTrustees_header"))))
    }
  }

  private[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] = {
    def variationDeclarationLink(userAnswers: UserAnswers, srn: Option[String]): Option[TaskListLink] = {
      if (userAnswers.isUserAnswerUpdated) {
        Some(TaskListLink(declarationLinkText,
          if (userAnswers.areVariationChangesCompleted)
            controllers.routes.VariationDeclarationController.onPageLoad(srn).url
          else
            controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url
        ))
      } else {
        None
      }
    }
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
  }

  override def taskList: SchemeDetailsTaskList = {
    val schemeName = answers.get(SchemeNameId).getOrElse("")
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      Message("messages__schemeTaskList__about_scheme_header", schemeName),
      aboutSection(answers),
      None,
      addEstablisherHeader(answers, UpdateMode, srn),
      establishersSection(answers, UpdateMode, srn),
      addTrusteeHeader(answers, UpdateMode, srn),
      trusteesSection(answers, UpdateMode, srn),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      Message("messages__scheme_details__title"),
      Some(Message("messages__schemeTaskList__scheme_information_link_text")),
      Message("messages__scheme_details__title"),
      srn
    )
  }
}

object HsTaskListHelperVariations {
  private def aboutMembersViewLinkText(schemeName:String): Message =
    Message("messages__schemeTaskList__about_members_link_text_view", schemeName)
  private def aboutBenefitsAndInsuranceViewLinkText(schemeName:String): Message =
    Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName)
  private def viewEstablisherLinkText: Message =
    Message("messages__schemeTaskList__sectionEstablishers_view_link")
  private def viewTrusteesLinkText: Message =
    Message("messages__schemeTaskList__sectionTrustees_view_link")
  private def noEstablishersText: Message =
    Message("messages__schemeTaskList__sectionEstablishers_no_establishers")
  private def noTrusteesText: Message =
    Message("messages__schemeTaskList__sectionTrustees_no_trustees")

  private def typeOfTrusteeLink(linkText: Message, trusteeCount: Int, srn: Option[String], mode: Mode): Option[TaskListLink] =
    Some(TaskListLink(linkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, trusteeCount, srn).url))

  private def typeOfEstablisherLink(linkText: Message, establisherCount: Int, srn: Option[String], mode: Mode): Option[TaskListLink] =
    Some(TaskListLink(linkText, controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, establisherCount, srn).url))

  private def addEstablisherLink(linkText: Message, srn: Option[String], mode: Mode): Option[TaskListLink] =
    Some(TaskListLink(linkText, controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url))
}
