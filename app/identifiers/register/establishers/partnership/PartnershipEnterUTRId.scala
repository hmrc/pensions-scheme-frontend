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

package identifiers.register.establishers.partnership

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartnership, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class PartnershipEnterUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipEnterUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(PartnershipNoUTRReasonId(this.index))
}

object PartnershipEnterUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnershipEnterUTRId] = {

    new CheckYourAnswersPartnership[PartnershipEnterUTRId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__enterUTR"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_unique_taxpayer_reference"))
      }
      override def row(id: PartnershipEnterUTRId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        ReferenceValueCYA[PartnershipEnterUTRId](label, hiddenLabel)().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipEnterUTRId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, ua)
          case _ =>
            ReferenceValueCYA[PartnershipEnterUTRId](label, hiddenLabel)().updateRow(id)(changeUrl, ua)
        }
      }
    }
  }
}











