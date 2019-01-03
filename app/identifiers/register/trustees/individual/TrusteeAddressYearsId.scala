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

package identifiers.register.trustees.individual

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeCompleteId, TrusteesId}
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}

case class TrusteeAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.remove(
          IndividualPreviousAddressPostCodeLookupId(this.index))
          .flatMap(_.remove(TrusteePreviousAddressId(this.index)))
          .flatMap(_.remove(TrusteePreviousAddressListId(this.index)))
      case Some(AddressYears.UnderAYear) =>
        userAnswers.set(IsTrusteeCompleteId(index))(false)
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeAddressYearsId {
  override def toString: String = "trusteeAddressYears"

  implicit val cya: CheckYourAnswers[TrusteeAddressYearsId] =
    AddressYearsCYA(
      label = "messages__trusteeAddressYears_cya_label",
      changeAddressYears = "messages__visuallyhidden__trustee__address_years"
    )()
}
