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

package identifiers.register.establishers.individual

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, CheckYourAnswersIndividual, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class PreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ PreviousAddressId.toString
}

object PreviousAddressId {
  override def toString: String = "previousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PreviousAddressId] = {

    new CheckYourAnswersIndividual[PreviousAddressId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__previousAddressFor"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_previousAddress"))
      }

      override def row(id: PreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, changeAddress) = getLabel(id.index, ua)
        AddressCYA(label, changeAddress)().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, changeAddress) = getLabel(id.index, ua)
        PreviousAddressCYA(label,
          changeAddress,
          ua.get(IsEstablisherNewId(id.index)),
          ua.get(IndividualConfirmPreviousAddressId(id.index))
        )().updateRow(id)(changeUrl, ua)
      }
    }
  }
}
