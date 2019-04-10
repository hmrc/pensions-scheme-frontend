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

import identifiers._
import models.{Link, UpdateMode}
import play.api.i18n.Messages
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers, srn: String)(implicit messages: Messages) extends HsTaskListHelper(answers) {

  override protected[utils] def aboutSection(userAnswers: UserAnswers): Seq[SchemeDetailsTaskListSection] = {

    val membersLink = Link(aboutMembersLinkText, controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, Some(srn)).url)

    val benefitsAndInsuranceLink = Link(aboutBenefitsAndInsuranceLinkText,
      controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, Some(srn)).url)

    Seq(SchemeDetailsTaskListSection(userAnswers.get(IsAboutMembersCompleteId), membersLink, None),
      SchemeDetailsTaskListSection(userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId), benefitsAndInsuranceLink, None))
  }

  def taskList: SchemeDetailsTaskList = {
    SchemeDetailsTaskList(
      beforeYouStartSection(answers, UpdateMode, Some(srn)),
      aboutSection(answers),
      None,
      addEstablisherHeader(answers, UpdateMode, Some(srn)),
      establishers(answers, UpdateMode, Some(srn)),
      addTrusteeHeader(answers, UpdateMode, Some(srn)),
      trustees(answers, UpdateMode, Some(srn)),
      declarationLink(answers),
      answers.get(SchemeNameId).getOrElse(""),
      messages("messages__scheme_details__title"),
      messages("messages__scheme_details__title")
    )
  }

  override def declarationEnabled(userAnswers: UserAnswers): Boolean = {
    val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
    Seq(
      userAnswers.get(IsBeforeYouStartCompleteId),
      userAnswers.get(IsAboutMembersCompleteId),
      userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
      Some(isAllEstablishersCompleted(userAnswers, UpdateMode)),
      Some(isTrusteeOptional | isAllTrusteesCompleted(userAnswers, UpdateMode))
    ).forall(_.contains(true)) && userAnswers.isUserAnswerUpdated()
  }

}
