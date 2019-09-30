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

package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class PartnershipAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.remove(PartnershipPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(PartnershipPreviousAddressId(this.index)))
          .flatMap(_.remove(PartnershipPreviousAddressListId(this.index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnershipAddressYearsId {
  override def toString: String = "partnershipAddressYears"

  val label = "messages__partnership__checkYourAnswers__addressYears"
  val changeAddressYears = "messages__visuallyhidden__partnership__address_years"

  implicit val cya: CheckYourAnswers[PartnershipAddressYearsId] = {

    new CheckYourAnswers[PartnershipAddressYearsId] {
      override def row(id: PartnershipAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(label, changeAddressYears)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => AddressYearsCYA(label, changeAddressYears)().row(id)(changeUrl, userAnswers)
          case _ => AddressYearsCYA(label, changeAddressYears)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
