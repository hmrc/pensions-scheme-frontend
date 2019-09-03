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
import identifiers.register.trustees.TrusteesId
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipAddressId.toString
}

object PartnershipAddressId {
  override def toString: String = "partnershipAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua: UserAnswers): CheckYourAnswers[PartnershipAddressId] =

    new CheckYourAnswers[PartnershipAddressId] {
      override def row(id: PartnershipAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        def trusteeName(index: Int) = ua.get(PartnershipDetailsId(index)).fold(messages("messages__theTrustee"))(_.name)
        def label(index: Int) = messages("messages__addressFor", trusteeName(index))
        def changeAddress(index: Int) = messages("messages__changeTrusteeAddress", trusteeName(index))

        AddressCYA(
          label = label(id.index),
          changeAddress = changeAddress(id.index)
        )().row(id)(changeUrl, ua)
      }
      override def updateRow(id: PartnershipAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, ua)
    }
}
