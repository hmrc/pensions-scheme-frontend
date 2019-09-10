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

package identifiers.register.trustees.company

import identifiers._
import identifiers.register.trustees.TrusteesId
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json._
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ CompanyAddressId.toString
}

object CompanyAddressId {
  override def toString: String = "companyAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages): CheckYourAnswers[CompanyAddressId] =
    new CheckYourAnswers[CompanyAddressId] {

      override def row(id: CompanyAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val trusteeName = ua.get(CompanyDetailsId(id.index)).fold(messages("messages__theTrustee"))(_.companyName)
        val label = messages("messages__trusteeAddress", trusteeName)
        val changeAddress = messages("messages__visuallyhidden__dynamic_address", trusteeName)

        AddressCYA(
          label = label,
          changeAddress = changeAddress
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: CompanyAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, ua)
    }
}
