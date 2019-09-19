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

package identifiers.register.trustees.individual

import config.FeatureSwitchManagementService
import identifiers.TypedIdentifier
import identifiers.register.trustees.TrusteesId
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, Toggles, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeAddressId.toString
}

object TrusteeAddressId {
  override lazy val toString: String = "trusteeAddressId"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua: UserAnswers,
                   featureSwitchManagementService: FeatureSwitchManagementService): CheckYourAnswers[TrusteeAddressId] =
    new CheckYourAnswers[TrusteeAddressId] {
      override def row(id: TrusteeAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val name = (index: Int) =>
            ua.get(TrusteeNameId(index)).map(_.fullName)

        val trusteeName = name(id.index).getOrElse(messages("messages__theTrustee"))

        val label = messages("messages__trusteeAddress", trusteeName)

        val changeAddress = messages("messages__visuallyhidden__dynamic_address", trusteeName)

        AddressCYA(
          label,
          changeAddress
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: TrusteeAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, ua)
    }
}
