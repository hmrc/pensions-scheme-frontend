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
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId, IsEstablisherNewId}
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class CompanyAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = EstablishersId(index).path \ CompanyAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers
          .remove(CompanyPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(CompanyPreviousAddressId(this.index)))
          .flatMap(_.remove(CompanyPreviousAddressListId(this.index)))
      case Some(AddressYears.UnderAYear) =>
        userAnswers.set(IsCompanyCompleteId(index))(false).flatMap(
          _.set(IsEstablisherCompleteId(index))(false).flatMap(
            _.set(IsAddressCompleteId(index))(false)
          )
        )
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object CompanyAddressYearsId {
  override lazy val toString: String = "companyAddressYears"

  implicit val cya: CheckYourAnswers[CompanyAddressYearsId] = {
    val label: String = "companyAddressYears.checkYourAnswersLabel"
    val changeAddressYears: String = "messages__visuallyhidden__establisher__address_years"

    new CheckYourAnswers[CompanyAddressYearsId] {
      override def row(id: CompanyAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(label, changeAddressYears)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => AddressYearsCYA(label, changeAddressYears)().row(id)(changeUrl, userAnswers)
          case _ => AddressYearsCYA(label, changeAddressYears)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
