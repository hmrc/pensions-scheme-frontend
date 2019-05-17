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
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipPreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipPreviousAddressId.toString
}

object PartnershipPreviousAddressId {
  override def toString: String = "partnershipPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnershipPreviousAddressId] = {
    val label: String = "messages__common__cya__previous_address"
    val changeAddress: String = "messages__visuallyhidden__partnership__previous_address"
    val previousAddressAddLabel: String = "messages__visuallyhidden__partnership__previous_address_add"

    new CheckYourAnswers[PartnershipPreviousAddressId] {
      override def row(id: PartnershipPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PreviousAddressCYA(label,
          changeAddress,
          userAnswers.get(IsTrusteeNewId(id.index)),
          userAnswers.get(PartnershipAddressYearsId(id.index)),
          Some(previousAddressAddLabel))().updateRow(id)(changeUrl, userAnswers)
    }
  }
}
