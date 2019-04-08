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

package utils

import identifiers.{IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId}
import models.{Link, NormalMode}
import play.api.i18n.Messages
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers)(implicit messages: Messages) extends HsTaskListHelper(answers) {

  override protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {
    val membersLink = userAnswers.get(IsAboutMembersCompleteId) match {
      case Some(true) => Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None).url)
      case _ => Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url)
    }

    val benefitsAndInsuranceLink = userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId) match {
      case Some(true) => Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, None).url)
      case _ => Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url)
    }

    Seq(SchemeDetailsTaskListSection(userAnswers.get(IsAboutMembersCompleteId), membersLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId), benefitsAndInsuranceLink, None))
  }

  def taskList: SchemeDetailsTaskList = {
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      aboutSection(answers),
      None,
      addEstablisherHeader(answers),
      establishers(answers),
      addTrusteeHeader(answers),
      trustees(answers),
      declarationLink(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      messages("messages__scheme_details__title")
    )
  }

}
