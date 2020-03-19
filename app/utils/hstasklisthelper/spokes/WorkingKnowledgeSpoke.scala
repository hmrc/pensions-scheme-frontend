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

package utils.hstasklisthelper.spokes

import models.{Index, Mode, TaskListLink}
import utils.UserAnswers
import viewmodels.Message

case object WorkingKnowledgeSpoke extends Spoke {
  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
    Message("messages__schemeTaskList__add_details_wk"), controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      Message("messages__schemeTaskList__change_details", name), controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
     Message("messages__schemeTaskList__change_details", name), controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url
  )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] = answers.isAdviserCompleted
}



