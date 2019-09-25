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

package identifiers.register.establishers.partnership.partner

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnerPreviousAddressId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerPreviousAddressId.toString
}

object PartnerPreviousAddressId {
  override def toString: String = "partnerPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnerPreviousAddressId] = {
    val label: String = "messages__common__cya__previous_address"
    val changeAddress: String = "messages__visuallyhidden__partner__previous_address"

    new CheckYourAnswers[PartnerPreviousAddressId] {
      override def row(id: PartnerPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PreviousAddressCYA(label,
          changeAddress,
          userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)),
          userAnswers.get(PartnerConfirmPreviousAddressId(id.establisherIndex, id.partnerIndex))
        )().updateRow(id)(changeUrl, userAnswers)
    }
  }
}
