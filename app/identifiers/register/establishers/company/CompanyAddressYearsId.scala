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

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.{AnswerRow, Message}

case class CompanyAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = EstablishersId(index).path \ CompanyAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers
          .remove(CompanyPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(CompanyPreviousAddressId(this.index)))
          .flatMap(_.remove(CompanyPreviousAddressListId(this.index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object CompanyAddressYearsId {
  override lazy val toString: String = "companyAddressYears"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[CompanyAddressYearsId] = {
    def label(ua: UserAnswers, index: Int): Message = ua.get(CompanyDetailsId(index)).map(details =>
      Message("messages__company_address_years__h1", details.companyName)).getOrElse(Message("messages__company_address_years__title"))

    val changeAddressYears: String = "messages__visuallyhidden__establisher__address_years"

    new CheckYourAnswers[CompanyAddressYearsId] {
      override def row(id: CompanyAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(label(userAnswers, id.index), changeAddressYears)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => AddressYearsCYA(label(userAnswers, id.index), changeAddressYears)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
