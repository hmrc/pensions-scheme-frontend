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
import models.{EntitySpoke, Mode, NormalMode, TaskListLink}
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperRegistration(answers: UserAnswers) extends HsTaskListHelper(answers) {
  import HsTaskListHelperRegistration._

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(
      None,
      Seq(
        EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__before_you_start_link_text", schemeName),
          if (answers.isBeforeYouStartCompleted(NormalMode)) {
            controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, None).url
          } else {
            controllers.routes.SchemeNameController.onPageLoad(NormalMode).url
          }
        ),
          Some(answers.isBeforeYouStartCompleted(NormalMode))
        )
      ),
      Some(Message("messages__schemeTaskList__before_you_start_header"))
    )
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers,
                                          mode: Mode,
                                          srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    if (userAnswers.allEstablishersAfterDelete(mode).isEmpty) {
      Some(
        SchemeDetailsTaskListHeader(
          None,
          Some(TaskListLink(
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
            TaskListLink(changeEstablisherLinkText, controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url)),
          None))
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListHeader] = {
    (userAnswers.get(HaveAnyTrusteesId), userAnswers.allTrusteesAfterDelete.isEmpty) match {
      case (None | Some(true), false) =>
        Some(
          SchemeDetailsTaskListHeader(
            None,
            Some(TaskListLink(changeTrusteesLinkText, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)),
            None))

      case (None | Some(true), true) =>
        Some(
          SchemeDetailsTaskListHeader(
            None,
            Some(TaskListLink(addTrusteesLinkText,
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
          case Some(true)  => TaskListLink(workingKnowledgeLinkText, controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url)
          case Some(false) => TaskListLink(workingKnowledgeLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
          case None        => TaskListLink(workingKnowledgeAddLinkText, controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url)
        }
        Some(SchemeDetailsTaskListSection(userAnswers.isWorkingKnowledgeCompleted, wkLink, None))
      case _ =>
        None
    }

  private[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListDeclarationSection] = {
    def declarationLink(userAnswers: UserAnswers): Option[TaskListLink] =
      if (declarationEnabled(userAnswers))
        Some(TaskListLink(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url))
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
      aboutSection(answers, NormalMode, None),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers, NormalMode, None),
      establishersSection(answers, NormalMode, None),
      addTrusteeHeader(answers, NormalMode, None),
      trusteesSection(answers, NormalMode, None),
      declarationSection(answers),
      answers.get(SchemeNameId).getOrElse(""),
      Message("messages__scheme_details__title"),
      Message("messages__schemeTaskList__title"),
      None
    )
}

object HsTaskListHelperRegistration {
  private def changeEstablisherLinkText: Message =
    Message("messages__schemeTaskList__sectionEstablishers_change_link")
  private def changeTrusteesLinkText: Message =
    Message("messages__schemeTaskList__sectionTrustees_change_link")
  private def workingKnowledgeAddLinkText: Message = Message("messages__schemeTaskList__add_details_wk")
  private def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean =
    userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)

  private def isAllEstablishersCompleted(userAnswers: UserAnswers, mode: Mode): Boolean =
    userAnswers.allEstablishersAfterDelete(mode).nonEmpty &&
      userAnswers.allEstablishersAfterDelete(mode).forall(_.isCompleted)

  private def declarationEnabled(userAnswers: UserAnswers): Boolean =
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
