/*
 * Copyright 2024 HM Revenue & Customs
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
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersTrusteeIndividual}
import viewmodels.{AnswerRow, Message}

case class TrusteeAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.remove(
          IndividualPreviousAddressPostCodeLookupId(this.index))
          .flatMap(_.remove(TrusteePreviousAddressId(this.index)))
          .flatMap(_.remove(TrusteePreviousAddressListId(this.index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeAddressYearsId {
  override def toString: String = "trusteeAddressYears"

  implicit def cya: CheckYourAnswers[TrusteeAddressYearsId] =
    new CheckYourAnswersTrusteeIndividual[TrusteeAddressYearsId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__trusteeAddressYears__heading"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_addressYears"))
      }

      override def row(id: TrusteeAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        AddressYearsCYA(
          label,
          hiddenLabel
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: TrusteeAddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Nil
        }
    }
}
