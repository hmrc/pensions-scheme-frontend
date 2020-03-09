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

import identifiers._
import identifiers.register.trustees.MoreThanTenTrusteesId
import models.{Link, Mode, NormalMode}
import play.api.i18n.Messages
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperRegistration(answers: UserAnswers)(implicit messages: Messages) extends HsTaskListHelper(answers) {
  import HsTaskListHelperRegistration._

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListSection = {
    SchemeDetailsTaskListSection(
      isCompleted = Some(answers.isBeforeYouStartCompleted(NormalMode)),
      link = Link(
        messages("messages__schemeTaskList__before_you_start_link_text", schemeName),
        if (answers.isBeforeYouStartCompleted(NormalMode)) {
          controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, None).url
        } else {
          controllers.routes.SchemeNameController.onPageLoad(NormalMode).url
        }
      ),
      header = None
    )
  }

  private[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.isMembersCompleted match {
      case Some(true) =>
        Link(aboutMembersLinkText(schemeName), controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None).url)
      case Some(false) => Link(aboutMembersLinkText(schemeName), controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
      case None        => Link(aboutMembersAddLinkText(schemeName), controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = userAnswers.isBenefitsAndInsuranceCompleted match {
      case Some(true) =>
        Link(aboutBenefitsAndInsuranceLinkText(schemeName),
             controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, None).url)
      case Some(false) =>
        Link(aboutBenefitsAndInsuranceLinkText(schemeName), controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url)
      case None =>
        Link(aboutBenefitsAndInsuranceAddLinkText(schemeName),
             controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url)
    }

    val bankDetailsLink = userAnswers.isBankDetailsCompleted match {
      case Some(true) =>
        Link(aboutBankDetailsLinkText(schemeName), controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad().url)
      case Some(false) =>
        Link(aboutBankDetailsLinkText(schemeName), controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url)
      case None => Link(aboutBankDetailsAddLinkText(schemeName), controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url)
    }

    Seq(
      SchemeDetailsTaskListSection(userAnswers.isMembersCompleted, membersLink, None),
      SchemeDetailsTaskListSection(userAnswers.isBenefitsAndInsuranceCompleted, benefitsAndInsuranceLink, None),
      SchemeDetailsTaskListSection(userAnswers.isBankDetailsCompleted, bankDetailsLink, None)
    )
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers,
                                          mode: Mode,
                                          srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    if (userAnswers.allEstablishersAfterDelete(mode).isEmpty) {
      Some(
        SchemeDetailsTaskListHeader(
          None,
          Some(Link(
            addEstablisherLinkText,
            controllers.register.establishers.routes.EstablisherKindController
              .onPageLoad(mode, userAnswers.allEstablishers(mode).size, srn)
              .url
          )),
          None
        ))
    } else {
      Some(
        SchemeDetailsTaskListHeader(
          None,
          Some(
            Link(changeEstablisherLinkText, controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url)),
          None))
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    (userAnswers.get(HaveAnyTrusteesId), userAnswers.allTrusteesAfterDelete.isEmpty) match {
      case (None | Some(true), false) =>
        Some(
          SchemeDetailsTaskListHeader(
            None,
            Some(Link(changeTrusteesLinkText, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)),
            None))

      case (None | Some(true), true) =>
        Some(
          SchemeDetailsTaskListHeader(
            None,
            Some(Link(addTrusteesLinkText,
                      controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url)),
            None
          ))

      case _ =>
        None
    }
  }

  private[utils] def workingKnowledgeSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListSection] =
    userAnswers.get(DeclarationDutiesId) match {
      case Some(false) =>
        val wkLink = userAnswers.isAdviserCompleted match {
          case Some(true)  => Link(workingKnowledgeLinkText, controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url)
          case Some(false) => Link(workingKnowledgeLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
          case None        => Link(workingKnowledgeAddLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
        }
        Some(SchemeDetailsTaskListSection(userAnswers.isWorkingKnowledgeCompleted, wkLink, None))
      case _ =>
        None
    }

  private[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] = {
    def declarationLink(userAnswers: UserAnswers): Option[Link] =
      if (declarationEnabled(userAnswers))
        Some(Link(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url))
      else None
    Some(
      SchemeDetailsTaskListDeclarationSection(
        header = "messages__schemeTaskList__sectionDeclaration_header",
        declarationLink = declarationLink(userAnswers),
        incompleteDeclarationText = "messages__schemeTaskList__sectionDeclaration_incomplete"
      ))
  }

  override def taskList: SchemeDetailsTaskList =
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      messages("messages__schemeTaskList__about_scheme_header", schemeName),
      aboutSection(answers),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers, NormalMode, None),
      establishersSection(answers, NormalMode, None),
      addTrusteeHeader(answers, NormalMode, None),
      trusteesSection(answers, NormalMode, None),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      Some(messages("messages__schemeTaskList__before_you_start_header")),
      messages("messages__schemeTaskList__title"),
      None
    )
}

object HsTaskListHelperRegistration {
  private def aboutMembersLinkText(schemeName: String)(implicit messages: Messages): String =
    messages("messages__schemeTaskList__about_members_link_text", schemeName)
  private def aboutMembersAddLinkText(schemeName: String)(implicit messages: Messages): String =
    messages("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private def aboutBenefitsAndInsuranceLinkText(schemeName: String)(implicit messages: Messages): String =
    messages("messages__schemeTaskList__about_benefits_and_insurance_link_text", schemeName)
  private def aboutBenefitsAndInsuranceAddLinkText(schemeName: String)(implicit messages: Messages): String =
    messages("messages__schemeTaskList__about_benefits_and_insurance_link_text_add", schemeName)
  private def aboutBankDetailsLinkText(schemeName: String)(implicit messages: Messages): String =
    messages("messages__schemeTaskList__about_bank_details_link_text", schemeName)
  private def aboutBankDetailsAddLinkText(schemeName: String)(implicit messages: Messages): String =
    messages("messages__schemeTaskList__about_bank_details_link_text_add", schemeName)
  private def changeEstablisherLinkText(implicit messages: Messages): String =
    messages("messages__schemeTaskList__sectionEstablishers_change_link")
  private def changeTrusteesLinkText(implicit messages: Messages): String =
    messages("messages__schemeTaskList__sectionTrustees_change_link")
  private def workingKnowledgeAddLinkText(implicit messages: Messages): String = messages("messages__schemeTaskList__add_details_wk")
  private def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean =
    userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)

  private def isAllEstablishersCompleted(userAnswers: UserAnswers, mode: Mode): Boolean =
    userAnswers.allEstablishersAfterDelete(mode).nonEmpty &&
      userAnswers.allEstablishersAfterDelete(mode).forall(_.isCompleted)

  def declarationEnabled(userAnswers: UserAnswers): Boolean =
    Seq(
      Some(userAnswers.isBeforeYouStartCompleted(NormalMode)),
      userAnswers.isMembersCompleted,
      userAnswers.isBankDetailsCompleted,
      userAnswers.isBenefitsAndInsuranceCompleted,
      userAnswers.isWorkingKnowledgeCompleted,
      Some(isAllEstablishersCompleted(userAnswers, NormalMode)),
      Some(userAnswers.get(HaveAnyTrusteesId).contains(false) | isAllTrusteesCompleted(userAnswers)),
      Some(userAnswers.allTrusteesAfterDelete.size < 10 || userAnswers.get(MoreThanTenTrusteesId).isDefined)
    ).forall(_.contains(true))
}
