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

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.{Link, VatNew}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, VatNewCYA}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipVatVariationsId(index: Int) extends TypedIdentifier[VatNew] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipVatVariationsId.toString
}

object PartnershipVatVariationsId {
  override def toString: String = "partnershipVat"

  val hiddenLabelVat = "messages__visuallyhidden__partnership__vat_number"
  val vatLabel = "messages__common__cya__vat"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnershipVatVariationsId] = {
    new CheckYourAnswers[PartnershipVatVariationsId] {

      override def row(id: PartnershipVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        VatNewCYA[PartnershipVatVariationsId](vatLabel, hiddenLabelVat)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: PartnershipVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            VatNewCYA[PartnershipVatVariationsId](vatLabel, hiddenLabelVat)().row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id) match {
              case Some(vatNew) => Seq(AnswerRow(vatLabel, Seq(vatNew.vat), answerIsMessageKey = false, None))
              case _ => Seq(AnswerRow(vatLabel, Seq("site.not_entered"), answerIsMessageKey = true,
                Some(Link("site.add", changeUrl, Some(s"${hiddenLabelVat}_add")))))
            }
        }
    }
  }
}







