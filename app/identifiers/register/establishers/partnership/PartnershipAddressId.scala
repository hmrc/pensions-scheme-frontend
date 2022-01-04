/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, CheckYourAnswersPartnership}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class PartnershipAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipAddressId.toString
}

object PartnershipAddressId {
  override def toString: String = "partnershipAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnershipAddressId] =

    new CheckYourAnswersPartnership[PartnershipAddressId] {
      private def label(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__address__cya")

      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_address")

      override def row(id: PartnershipAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        AddressCYA(
          label = label(id.index, ua),
          changeAddress = hiddenLabel(id.index, ua)
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        row(id)(changeUrl, ua)
    }
}
