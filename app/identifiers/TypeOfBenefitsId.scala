/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{Link, TypeOfBenefits}
import play.api.libs.json.Reads
import utils.checkyouranswers.CheckYourAnswers
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerRow, Message}

case object TypeOfBenefitsId extends TypedIdentifier[TypeOfBenefits] with Enumerable.Implicits {
  self =>
  override def toString: String = "benefits"

  implicit def cya(implicit userAnswers: UserAnswers,
                   rds: Reads[TypeOfBenefits]): CheckYourAnswers[self.type] = {
    new CheckYourAnswers[self.type] {
    val label = Message("messages__type_of_benefits_cya_label", userAnswers.get(SchemeNameId).getOrElse(""))
    val hiddenLabel = Some(Message("messages__visuallyhidden__type_of_benefits_change", userAnswers.get(SchemeNameId).getOrElse("")))
      private def typeOfBenefitsCYARow(id: self.type , userAnswers: UserAnswers, changeUrl: Option[Link]): Seq[AnswerRow] = {
        userAnswers.get(id).map {
          typeOfBenefits =>
            Seq(
              AnswerRow(
                label,
                Seq(s"messages__type_of_benefits__$typeOfBenefits"),
                answerIsMessageKey = true,
                changeUrl
              )
            )
        }.getOrElse(Seq.empty[AnswerRow])
      }

      override def row(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        typeOfBenefitsCYARow(id, userAnswers,
        Some(Link("site.change", changeUrl, hiddenLabel)))

      override def updateRow(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        typeOfBenefitsCYARow(id, userAnswers, None)
    }
  }
}
