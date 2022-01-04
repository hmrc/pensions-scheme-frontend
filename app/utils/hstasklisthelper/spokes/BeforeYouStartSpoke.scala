/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.hstasklisthelper.spokes

import models.{Index, Mode, TaskListLink}
import utils.UserAnswers

case object BeforeYouStartSpoke extends Spoke {
  private val registrationLinkText = "messages__schemeTaskList__before_you_start_link_text"
  private val variationsLinkText = "messages__schemeTaskList__scheme_info_link_text"

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationLinkText, variationsLinkText),
      controllers.routes.SchemeNameController.onPageLoad(mode).url
    )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationLinkText, variationsLinkText),
      controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(mode, srn).url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationLinkText, variationsLinkText),
      controllers.routes.SchemeNameController.onPageLoad(mode).url
    )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] = Some(answers
    .isBeforeYouStartCompleted(mode))
}



