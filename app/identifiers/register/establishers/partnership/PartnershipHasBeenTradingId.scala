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

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnershipHasBeenTradingId] =
    new CheckYourAnswers[PartnershipHasBeenTradingId] {

      override def row(id: PartnershipHasBeenTradingId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val trusteeName = ua.get(PartnershipDetailsId(id.index)).fold(messages("messages__theTrustee"))(_.name)
        val label = messages("messages__hasBeenTrading__h1", trusteeName)
        val hiddenLabel = messages("messages__visuallyhidden__dynamic__hasBeenTrading", trusteeName)

        BooleanCYA(Some(label), Some(hiddenLabel))().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipHasBeenTradingId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Seq.empty[AnswerRow]
        }
    }
}











