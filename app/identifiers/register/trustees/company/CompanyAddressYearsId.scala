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

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = TrusteesId(index).path \ CompanyAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.removeAllOf(List(
          CompanyPreviousAddressPostcodeLookupId(index),
          CompanyPreviousAddressId(index),
          CompanyPreviousAddressListId(index),
          HasBeenTradingCompanyId(index)
        ))
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object CompanyAddressYearsId {
  override lazy val toString: String = "trusteesCompanyAddressYears"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua: UserAnswers): CheckYourAnswers[CompanyAddressYearsId] =
    new CheckYourAnswers[CompanyAddressYearsId] {
      override def row(id: CompanyAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        def trusteeName(index: Int) = ua.get(CompanyDetailsId(index)).fold(messages("messages__theTrustee"))(_.companyName)

        def label(index: Int) = messages("messages__hasBeen1Year", trusteeName(index))

        def changeAddressYears(index: Int) = messages("messages__visuallyhidden__dynamic_addressYears", trusteeName(index))

        AddressYearsCYA(
          label = label(id.index),
          changeAddressYears = changeAddressYears(id.index)
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: CompanyAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Nil
        }
    }
}
