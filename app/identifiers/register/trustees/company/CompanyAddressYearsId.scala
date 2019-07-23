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
import identifiers.register.trustees.{IsTrusteeCompleteId, IsTrusteeNewId, TrusteesId}
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class CompanyAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = TrusteesId(index).path \ CompanyAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers
          .remove(CompanyPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(CompanyPreviousAddressId(this.index)))
          .flatMap(_.remove(CompanyPreviousAddressListId(this.index)))
      case Some(AddressYears.UnderAYear) =>
        userAnswers.set(IsTrusteeCompleteId(index))(false)
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object CompanyAddressYearsId {
  override lazy val toString: String = "trusteesCompanyAddressYears"

  implicit def cya(implicit messages: Messages, ua: UserAnswers): CheckYourAnswers[CompanyAddressYearsId] = {

    def label(index: Int) = ua.get(CompanyDetailsId(index)) match {
      case Some(details) => messages("messages__trusteeHowLongAtAddress__title", details.companyName)
      case _ => "messages__checkYourAnswers__trustees__company__address_years"
    }

    def changeAddressYears(index: Int) = ua.get(CompanyDetailsId(index)) match {
      case Some(details) => messages("messages__visuallyhidden__trustee__address", details.companyName)
      case _ => messages("messages__visuallyhidden__trustee__address_years")
    }

    new CheckYourAnswers[CompanyAddressYearsId] {
      override def row(id: CompanyAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(label(id.index), changeAddressYears(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => AddressYearsCYA(label(id.index), changeAddressYears(id.index))().row(id)(changeUrl, userAnswers)
          case _ => AddressYearsCYA(label(id.index), changeAddressYears(id.index))().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
