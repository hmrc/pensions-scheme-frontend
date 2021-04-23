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

import models.TypeOfBenefits.{MoneyPurchase, MoneyPurchaseDefinedMix}
import models.{Link, MoneyPurchaseBenefits}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.checkyouranswers.CheckYourAnswers
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerRow, Message}

case object MoneyPurchaseBenefitsId
  extends TypedIdentifier[MoneyPurchaseBenefits]
    with Enumerable.Implicits {
  self =>

  override def toString: String = "moneyPurchaseBenefits"

  implicit def cya(
                    implicit userAnswers: UserAnswers,
                    rds: Reads[MoneyPurchaseBenefits],
                    messages: Messages,
                    tcmpToggle: Boolean
                  ): CheckYourAnswers[self.type] = {
    new CheckYourAnswers[self.type] {

      val label: Message =
        Message("messages__moneyPurchaseBenefits__cya", userAnswers.get(SchemeNameId).getOrElse(""))
      val hiddenLabel: Option[Message] =
        Some(Message("messages__moneyPurchaseBenefits__cya_hidden", userAnswers.get(SchemeNameId).getOrElse("")))

      private def moneyPurchaseBenefitsCYARow(
                                               id: self.type,
                                               userAnswers: UserAnswers,
                                               changeUrl: Option[Link]
                                             ): Seq[AnswerRow] =
        userAnswers.get(id).map {
          moneyBenefits =>
            Seq(AnswerRow(
              label = label,
              answer = Seq(messages(s"messages__moneyPurchaseBenefits__$moneyBenefits")),
              answerIsMessageKey = false,
              changeUrl = changeUrl
            ))
        }.getOrElse(Seq.empty[AnswerRow])

      override def row(id: self.type)
                      (changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        moneyPurchaseBenefitsCYARow(
          id = id,
          userAnswers = userAnswers,
          changeUrl = Some(Link("site.change", changeUrl, hiddenLabel))
        )

      override def updateRow(id: self.type)
                            (changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        (userAnswers.get(id), userAnswers.get(TypeOfBenefitsId)) match {
          case (Some(_), _) =>
            row(id)(changeUrl, userAnswers)
          case (_, Some(MoneyPurchase) | Some(MoneyPurchaseDefinedMix)) if tcmpToggle =>
            Seq(AnswerRow(
              label = label,
              answer = Seq("site.not_entered"),
              answerIsMessageKey = true,
              changeUrl = Some(Link("site.add", changeUrl, hiddenLabel))
            ))
          case _ =>
            Seq.empty[AnswerRow]
        }
    }
  }
}
