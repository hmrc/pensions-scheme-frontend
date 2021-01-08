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

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersPartnership}
import viewmodels.{AnswerRow, Message}

case class PartnershipAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.remove(PartnershipPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(PartnershipPreviousAddressId(this.index)))
          .flatMap(_.remove(PartnershipPreviousAddressListId(this.index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnershipAddressYearsId {
  override def toString: String = "partnershipAddressYears"

  implicit def cya: CheckYourAnswers[PartnershipAddressYearsId] =
    new CheckYourAnswersPartnership[PartnershipAddressYearsId] {
      private def label(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__partnershipAddressYears__heading")

      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_addressYears")

      override def row(id: PartnershipAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        AddressYearsCYA(
          label = label(id.index, ua),
          changeAddressYears = hiddenLabel(id.index, ua)
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Nil
        }
    }
}
