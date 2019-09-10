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

package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class PartnershipAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.removeAllOf(List(
          PartnershipPreviousAddressPostcodeLookupId(index),
          PartnershipPreviousAddressId(index),
          PartnershipPreviousAddressListId(index),
          PartnershipHasBeenTradingId(index)
        ))
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object PartnershipAddressYearsId {
  override lazy val toString: String = "partnershipAddressYears"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua: UserAnswers): CheckYourAnswers[PartnershipAddressYearsId] =
    new CheckYourAnswers[PartnershipAddressYearsId] {
      override def row(id: PartnershipAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val trusteeName = ua.get(PartnershipDetailsId(id.index)).fold(messages("messages__theTrustee"))(_.name)
        val label = messages("messages__trusteeAddressYears__heading", trusteeName)
        val changeAddressYears = messages("messages__visuallyhidden__dynamic_addressYears", trusteeName)

        AddressYearsCYA(
          label = label,
          changeAddressYears = changeAddressYears
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Nil
        }
    }
}
