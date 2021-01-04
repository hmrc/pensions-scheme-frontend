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
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartnership}
import viewmodels.{AnswerRow, Message}

case class PartnershipHasBeenTradingId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipHasBeenTradingId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers
          .remove(PartnershipPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(PartnershipPreviousAddressId(this.index)))
          .flatMap(_.remove(PartnershipPreviousAddressListId(this.index)))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object PartnershipHasBeenTradingId {
  override def toString: String = "hasBeenTrading"

  implicit def cya: CheckYourAnswers[PartnershipHasBeenTradingId] =
    new CheckYourAnswersPartnership[PartnershipHasBeenTradingId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__hasBeenTrading__h1"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic__hasBeenTrading"))
      }
      override def row(id: PartnershipHasBeenTradingId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        BooleanCYA(Some(label), Some(hiddenLabel))().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipHasBeenTradingId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Seq.empty[AnswerRow]
        }
    }
}











