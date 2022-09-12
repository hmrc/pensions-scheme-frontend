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

import models.{Index, Mode, TaskListLink, UpdateMode}
import utils.UserAnswers
import viewmodels.Message

case object AboutMembersSpoke extends Spoke {
  private val variationsViewLinkText = "messages__schemeTaskList__about_members_link_text_view"
  private val registrationAddLinkText = "messages__schemeTaskList__about_members_link_text_add"
  private val registrationChangeLinkText = "messages__schemeTaskList__about_members_link_text"
  private val registrationContinueLinkText = "messages__schemeTaskList__about_members_link_text_continue"

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationAddLinkText, variationsViewLinkText),
      controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url
    )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationChangeLinkText, variationsViewLinkText),
      controllers.routes.CheckYourAnswersMembersController.onPageLoad(mode, srn).url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationContinueLinkText, variationsViewLinkText),
      controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url
    )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isMembersCompleted
}

case object AboutBenefitsAndInsuranceSpoke extends Spoke {
  private val variationsViewLinkText = "messages__schemeTaskList__about_benefits_and_insurance_link_text_view"
  private val registrationAddLinkText = "messages__schemeTaskList__about_benefits_and_insurance_link_text_add"
  private val registrationChangeLinkText = "messages__schemeTaskList__about_benefits_and_insurance_link_text"
  private val registrationContinueLinkText = "messages__schemeTaskList__about_benefits_and_insurance_link_text_continue"

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationAddLinkText, variationsViewLinkText),
      controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad.url
    )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationChangeLinkText, variationsViewLinkText),
      controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, srn).url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      dynamicLinkText(name, srn, registrationContinueLinkText, variationsViewLinkText),
      if (mode == UpdateMode)
        controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, srn).url
      else
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad.url
    )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isBenefitsAndInsuranceCompleted
}

case object AboutBankDetailsSpoke extends Spoke {
  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      Message("messages__schemeTaskList__about_bank_details_link_text_add", name),
      controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad.url
    )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      Message("messages__schemeTaskList__about_bank_details_link_text", name),
      controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad().url
    )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
      Message("messages__schemeTaskList__about_bank_details_link_text_continue", name),
      controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad.url
    )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isBankDetailsCompleted
}


