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

package identifiers.register.trustees.company

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json._
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyPreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ CompanyPreviousAddressId.toString
}

object CompanyPreviousAddressId {
  override def toString: String = "companyPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages)
  : CheckYourAnswers[CompanyPreviousAddressId] = {
    def trusteeName(index: Int, ua: UserAnswers): String = ua.get(CompanyDetailsId(index)).fold(messages
    ("messages__theTrustee"))(_.companyName)

    def label(index: Int, ua: UserAnswers) = messages("messages__trusteePreviousAddress", trusteeName(index, ua))

    def changeAddress(index: Int, ua: UserAnswers) = messages("messages__visuallyhidden__dynamic_previousAddress",
      trusteeName(index, ua))

    new CheckYourAnswers[CompanyPreviousAddressId] {
      override def row(id: CompanyPreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label(id.index, ua), changeAddress(id.index, ua))().row(id)(changeUrl, ua)

      override def updateRow(id: CompanyPreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        PreviousAddressCYA(label(id.index, ua),
          changeAddress(id.index, ua),
          ua.get(IsTrusteeNewId(id.index)),
          ua.get(CompanyConfirmPreviousAddressId(id.index))
        )().updateRow(id)(changeUrl, ua)
    }
  }
}
