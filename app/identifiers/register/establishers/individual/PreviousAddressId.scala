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

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.AddressYears.UnderAYear
import models.Link
import models.address.Address
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class PreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ PreviousAddressId.toString
}

object PreviousAddressId {
  override def toString: String = "previousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PreviousAddressId] = {
    val label: String = "messages__establisher_individual_previous_address_cya_label"
    val changeAddress: String = "messages__visuallyhidden__establisher__previous_address"

    new CheckYourAnswers[PreviousAddressId] {
      override def row(id: PreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id) match {
              case Some(_) => row(id)(changeUrl, userAnswers)
              case _ =>
                userAnswers.get(AddressYearsId(id.index)) match {
                  case Some(UnderAYear) => Seq(AnswerRow(label,
                    Seq("site.not_entered"),
                    answerIsMessageKey = true,
                    Some(Link("site.add", changeUrl, Some("messages__visuallyhidden__establisher__previous_address_add")))))
                  case _ => Seq.empty[AnswerRow]
                }
            }
        }
    }
  }
}
