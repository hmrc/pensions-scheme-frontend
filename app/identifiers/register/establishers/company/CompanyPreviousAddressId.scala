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

package identifiers.register.establishers.company

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyPreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ CompanyPreviousAddressId.toString
}

object CompanyPreviousAddressId {
  override def toString: String = "companyPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[CompanyPreviousAddressId] = {
    val label: String = "messages__common__cya__previous_address"
    val changeAddress: String = "messages__visuallyhidden__establisher__previous_address"
    val previousAddressAddLabel: String = "messages__visuallyhidden__establisher__previous_address_add"

    new CheckYourAnswers[CompanyPreviousAddressId] {
      override def row(id: CompanyPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PreviousAddressCYA(label,
          changeAddress,
          userAnswers.get(IsEstablisherNewId(id.index)),
          userAnswers.get(CompanyAddressYearsId(id.index)),
          Some(previousAddressAddLabel))().updateRow(id)(changeUrl, userAnswers)
    }
  }
}
