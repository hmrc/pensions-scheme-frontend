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

package identifiers.register.establishers.individual

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, CheckYourAnswersIndividual}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class AddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ AddressId.toString
}

object AddressId {
  override def toString: String = "address"

  implicit def cya(implicit countryOptions: CountryOptions, ua: UserAnswers): CheckYourAnswers[AddressId] =
    new CheckYourAnswersIndividual[AddressId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__addressFor"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_address"))
      }

      override def row(id: AddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        AddressCYA(
          label,
          hiddenLabel
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: AddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, ua)
    }
}
