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
import models._
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperPsp {

  def taskList(answers: UserAnswers, srn: String): PspTaskList =
    PspTaskList(
      answers.get(SchemeNameId).getOrElse(""),
      srn,
      beforeYouStartSection(answers, srn),
      aboutSection(answers, srn),
      establishersSection(answers),
      addTrusteeHeader(answers),
      trusteesSection(answers)
    )

  private def name(ua: UserAnswers): String = ua.get(SchemeNameId).getOrElse("")

  private def beforeYouStartSection(ua: UserAnswers, srn: String)
  : SchemeDetailsTaskListEntitySection =
    SchemeDetailsTaskListEntitySection(None,
      Seq(EntitySpoke(TaskListLink(
        Message("messages__schemeTaskList__scheme_info_link_text", name(ua)),
        controllers.routes.CheckYourAnswersBeforeYouStartController.pspOnPageLoad(srn).url
      ), None)),
      Some(Message("messages__schemeTaskList__scheme_information_link_text"))
    )

  private def aboutSection(ua: UserAnswers, srn: String)
  : SchemeDetailsTaskListEntitySection =
    SchemeDetailsTaskListEntitySection(None,
      Seq(EntitySpoke(TaskListLink(
        Message("messages__schemeTaskList__about_members_link_psp"),
        controllers.routes.CheckYourAnswersMembersController.pspOnPageLoad(srn).url
      ), None),
        EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__about_benefits_and_insurance_link_psp"),
          controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.pspOnPageLoad(srn).url
        ), None)),
      Some(Message("messages__schemeTaskList__about_scheme_header", name(ua)))
    )

  private def addTrusteeHeader(userAnswers: UserAnswers): Option[SchemeDetailsTaskListEntitySection] =
    if (userAnswers.allTrusteesAfterDelete.isEmpty)
      None
    else
      Some(SchemeDetailsTaskListEntitySection(None, Nil, None))

  protected def establishersSection(userAnswers: UserAnswers): Seq[String] =
    for ((establisher, _) <- userAnswers.allEstablishersAfterDelete(UpdateMode).zipWithIndex) yield establisher.name

  protected def trusteesSection(userAnswers: UserAnswers): Seq[String] =
    for ((trustee, _) <- userAnswers.allTrusteesAfterDelete.zipWithIndex) yield trustee.name
}
