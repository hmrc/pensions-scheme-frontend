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
    SchemeDetailsTaskListEntitySection(None,
      getBeforeYouStartSpoke(userAnswers, NormalMode, None, schemeName, None),
      Some(Message("messages__schemeTaskList__before_you_start_header"))
    )
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers,
                                          mode: Mode,
                                          srn: Option[String]): Option[SchemeDetailsTaskListEntitySection] = {
    if (userAnswers.allEstablishersAfterDelete(mode).isEmpty) {
      Some(
        SchemeDetailsTaskListEntitySection(None,
          Seq(EntitySpoke(
            TaskListLink(
              Message("messages__schemeTaskList__sectionEstablishers_add_link"),
              controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, userAnswers.allEstablishers(mode).size, srn).url
            ), None)), None
        )
      )
    } else {
      Some(
        SchemeDetailsTaskListEntitySection(None,
          Seq(EntitySpoke(
            TaskListLink(
              Message("messages__schemeTaskList__sectionEstablishers_change_link"),
              controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url),
            None
          )),
          None
        )
      )
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Option[SchemeDetailsTaskListEntitySection] = {
    (userAnswers.get(HaveAnyTrusteesId), userAnswers.allTrusteesAfterDelete.isEmpty) match {
      case (None | Some(true), false) =>
        Some(
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(
              TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link"),
                controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url),
              None
            )),
            None
          )
        )
      case (None | Some(true), true) =>
        Some(
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(
              TaskListLink(
                Message("messages__schemeTaskList__sectionTrustees_add_link"),
                controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url),
              None
            )),
            None
          )
        )
      case _ =>
        None
    }
  }

  private[utils] def workingKnowledgeSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListEntitySection] =
    userAnswers.get(DeclarationDutiesId) match {
      case Some(false) =>
        Some(SchemeDetailsTaskListEntitySection(None,
          getWorkingKnowledgeSpoke(userAnswers, NormalMode, None, schemeName, None),
          Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
        ))
      case _ =>
        None
    }

  private[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListEntitySection] = {
    def declarationLink: Seq[EntitySpoke] =
      if (declarationEnabled(userAnswers))
        Seq(EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__declaration_link"),
          controllers.register.routes.DeclarationController.onPageLoad().url)))
      else Nil

    Some(
      SchemeDetailsTaskListEntitySection(None,
        declarationLink,
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete"
      ))
  }

  override def taskList: SchemeDetailsTaskList =
    SchemeDetailsTaskList(
      answers.get(SchemeNameId).getOrElse(""),
      None,
      beforeYouStartSection(answers),
      aboutSection(answers, NormalMode, None),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers, NormalMode, None),
      establishersSection(answers, NormalMode, None),
      addTrusteeHeader(answers, NormalMode, None),
      trusteesSection(answers, NormalMode, None),
      declarationSection(answers)
    )
}

object HsTaskListHelperRegistration {

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
