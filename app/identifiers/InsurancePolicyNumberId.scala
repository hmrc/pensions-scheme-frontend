/*
 * Copyright 2023 HM Revenue & Customs
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

package identifiers

import models.Link
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case object InsurancePolicyNumberId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "insurancePolicyNumber"

  implicit def cya(implicit userAnswers: UserAnswers, countryOptions: CountryOptions): CheckYourAnswers[self.type] = {

      val label: Option[Message] = if (userAnswers.get(InsuranceCompanyNameId).isDefined) {
        Some(Message("messages__insurance_policy_number_cya_label", userAnswers.get(InsuranceCompanyNameId).getOrElse("")))
      } else {
        Some(Message("messages__insurance_policy_number__title"))
      }
      val hiddenLabel = if (userAnswers.get(InsuranceCompanyNameId).isDefined) {
        Some(Message("messages__visuallyhidden__insurance_policy_number", userAnswers.get(InsuranceCompanyNameId).getOrElse("")))
      } else {
        Some(Message("messages__visuallyhidden__insurance_policy_number_add"))
      }

    new CheckYourAnswers[self.type] {

      override def row(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        StringCYA[self.type](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(_) => row(id)(changeUrl, userAnswers)
          case _ => userAnswers.get(BenefitsSecuredByInsuranceId) match {
            case Some(true) => Seq(AnswerRow(
              label.fold(Message(s"${id.toString}.checkYourAnswersLabel"))(customLabel => customLabel),
              Seq("site.not_entered"),
              answerIsMessageKey = true,
              Some(Link("site.add", changeUrl, hiddenLabel))))
            case _ => Seq.empty[AnswerRow]
          }
        }
      }
    }
  }
}
