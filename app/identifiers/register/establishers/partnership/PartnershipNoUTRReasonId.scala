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
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class PartnershipNoUTRReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipNoUTRReasonId.toString
}

object PartnershipNoUTRReasonId {
  override def toString: String = "noUtrReason"

  implicit def cya(implicit messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[PartnershipNoUTRReasonId] = {

    new CheckYourAnswers[PartnershipNoUTRReasonId] {
      override def row(id: PartnershipNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val partnershipName = userAnswers.get(PartnershipDetailsId(id.index)).fold(messages
        ("messages__thePartnership"))(_.name)
        val label = Some(messages("messages__whyNoUTR", partnershipName))
        val hiddenLabel = Some(messages("messages__visuallyhidden__dynamic_noUtrReason", partnershipName))

        StringCYA(label, hiddenLabel)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: PartnershipNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers)
      : Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}










