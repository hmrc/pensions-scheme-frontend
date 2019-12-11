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

import identifiers._
import models.register.Entity
import models.{Link, Mode, NormalMode}
import play.api.i18n.Messages
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperRegistration(answers: UserAnswers
                                  )(implicit messages: Messages) extends HsTaskListHelper(answers) {


  override protected lazy val beforeYouStartLinkText: String = messages("messages__schemeTaskList__before_you_start_link_text", schemeName)
  def taskList: SchemeDetailsTaskList = {

    SchemeDetailsTaskList(
      SchemeDetailsTaskListSection(Some(answers.isBeforeYouStartCompleted(NormalMode)),
        beforeYouStartLink(answers, NormalMode, None), None),
      messages("messages__schemeTaskList__about_scheme_header", schemeName),
      aboutSection(answers),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers, NormalMode, None),
      establishers(answers, NormalMode, None),
      addTrusteeHeader(answers, NormalMode, None),
      trustees(answers, NormalMode, None),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      Some(messages("messages__schemeTaskList__before_you_start_header")),
      messages("messages__schemeTaskList__title"),
      None
    )
  }

  override protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.isMembersCompleted match {
      case Some(true) => Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None).url)
      case Some(false) => Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
      case None => Link(aboutMembersAddLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId) match {
      case Some(true) => Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, None).url)
      case Some(false) => Link(aboutBenefitsAndInsuranceLinkText, controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url)
      case None => Link(aboutBenefitsAndInsuranceAddLinkText, controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url)
    }

    val bankDetailsLink = userAnswers.get(IsAboutBankDetailsCompleteId) match {
      case Some(true) => Link(aboutBankDetailsLinkText, controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad().url)
      case Some(false) => Link(aboutBankDetailsLinkText, controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url)
      case None => Link(aboutBankDetailsAddLinkText, controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url)
    }

    Seq(SchemeDetailsTaskListSection(userAnswers.isMembersCompleted, membersLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId), benefitsAndInsuranceLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBankDetailsCompleteId), bankDetailsLink, None))
  }

  protected[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] =
    Some(SchemeDetailsTaskListDeclarationSection(
      header = "messages__schemeTaskList__sectionDeclaration_header",
      declarationLink = declarationLink(userAnswers),
      incompleteDeclarationText = "messages__schemeTaskList__sectionDeclaration_incomplete"))

  protected def listOf(sections: Seq[Entity[_]], userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        Some(SchemeDetailsTaskListSection(
          Some(section.isCompleted),
          Link(linkText(section),
            section.editLink(NormalMode, None).getOrElse(controllers.routes.SessionExpiredController.onPageLoad().url)),
          Some(section.name))
        )
      }
    }
    notDeletedElements.flatten
  }

  protected[utils] override def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    (userAnswers.get(HaveAnyTrusteesId), userAnswers.allTrusteesAfterDelete.isEmpty) match {
      case (None | Some(true), false) =>
        Some(
          SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None))

      case (None | Some(true), true) =>
        Some(
          SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode,
              userAnswers.allTrustees.size, srn).url)), None))

      case _ =>
        None
    }
  }

  protected[utils] def addEstablisherHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    if (userAnswers.allEstablishersAfterDelete(mode).isEmpty) {
      Some(SchemeDetailsTaskListHeader(None, Some(Link(addEstablisherLinkText,
        controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode,
          userAnswers.allEstablishers(mode).size, srn).url)), None))
    } else {
      Some(SchemeDetailsTaskListHeader(None, Some(Link(changeEstablisherLinkText,
        controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url)), None))
    }
  }

}
