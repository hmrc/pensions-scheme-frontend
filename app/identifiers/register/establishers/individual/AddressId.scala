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

package identifiers.register.establishers.individual

import config.FeatureSwitchManagementService
import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, Toggles, UserAnswers}
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class AddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ AddressId.toString
}

object AddressId {
  override def toString: String = "address"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua: UserAnswers,
                   featureSwitchManagementService: FeatureSwitchManagementService): CheckYourAnswers[AddressId] =
    new CheckYourAnswers[AddressId] {
      override def row(id: AddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val name = (index: Int) =>
          if(featureSwitchManagementService.get(Toggles.isHnSEnabled))
            ua.get(EstablisherNameId(index)).map(_.fullName)
          else
            ua.get(EstablisherDetailsId(index)).map(_.fullName)

        val establisherName = name(id.index).getOrElse(messages("messages__theEstablisher"))

        val label = messages("messages__addressFor", establisherName)

        val changeAddress = messages("messages__visuallyhidden__dynamic_address", establisherName)

        AddressCYA(
          label,
          changeAddress
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: AddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, ua)
    }
}
