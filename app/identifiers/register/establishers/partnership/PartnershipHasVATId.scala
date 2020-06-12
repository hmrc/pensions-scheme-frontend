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

package identifiers.register.establishers.partnership

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class PartnershipHasVATId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipHasVATId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(PartnershipEnterVATId(this.index))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object PartnershipHasVATId {
  override def toString: String = "hasVat"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnershipHasVATId] = {

    new CheckYourAnswers[PartnershipHasVATId] {
      override def row(id: PartnershipHasVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val partnershipName =
          userAnswers.get(PartnershipDetailsId(id.index))
            .fold(messages("messages__thePartnership"))(_.name)
        val label = Some(messages("messages__hasVAT", partnershipName))
        val hiddenLabel = Some(messages("messages__visuallyhidden__dynamic_hasVat", partnershipName))

        BooleanCYA(label, hiddenLabel)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: PartnershipHasVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}











