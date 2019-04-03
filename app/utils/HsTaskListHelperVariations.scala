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

import identifiers.{HaveAnyTrusteesId, IsAboutBankDetailsCompleteId, IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, IsBeforeYouStartCompleteId, IsWorkingKnowledgeCompleteId}
import play.api.i18n.Messages
import viewmodels._

class HsTaskListHelperVariations(answers: UserAnswers)(implicit messages: Messages) extends HsTaskListHelper(answers) {

  def taskList: SchemeDetailsTaskList = {
    SchemeDetailsTaskList(
      beforeYouStartSection(answers),
      aboutSection(answers),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers),
      establishers(answers),
      addTrusteeHeader(answers),
      trustees(answers),
      declarationLink(answers)
    )
  }

  override def declarationEnabled(userAnswers: UserAnswers): Boolean = {
      val isTrusteeOptional = userAnswers.get(HaveAnyTrusteesId).contains(false)
      Seq(
        userAnswers.get(IsBeforeYouStartCompleteId),
        userAnswers.get(IsAboutMembersCompleteId),
        userAnswers.get(IsAboutBenefitsAndInsuranceCompleteId),
        Some(isAllEstablishersCompleted(userAnswers)),
        Some(isTrusteeOptional | isAllTrusteesCompleted(userAnswers))
      ).forall(_.contains(true)) && userAnswers.isUserAnswerUpdated()
    }

}
