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

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersTrusteeCompany}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

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

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[CompanyAddressYearsId] =
    new CheckYourAnswersTrusteeCompany[CompanyAddressYearsId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__trusteeAddressYears__heading"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_addressYears"))
      }
      override def row(id: CompanyAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)

        AddressYearsCYA(
          label = label,
          changeAddressYears = hiddenLabel
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: CompanyAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Nil
        }
    }
}
