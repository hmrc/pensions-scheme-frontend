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

package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class PartnershipHasVATId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipHasVATId.toString

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

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[PartnershipHasVATId] = {

    def label(index: Int) = userAnswers.get(PartnershipDetailsId(index)) match {
      case Some(details) => Some(messages("messages__vat__heading", details.name))
      case _ => Some(messages("messages__vat__heading"))
    }

    def hiddenLabel = Some(messages("messages__visuallyhidden__partnership__vat_yes_no"))

    new CheckYourAnswers[PartnershipHasVATId] {
      override def row(id: PartnershipHasVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(label(id.index), hiddenLabel)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipHasVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => BooleanCYA(label(id.index), hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
