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

package identifiers.register.establishers.individual

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.AddressYears
import play.api.libs.json._
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersIndividual}
import viewmodels.{AnswerRow, Message}

case class AddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {

  override def path: JsPath = EstablishersId(index).path \ AddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers
          .remove(PreviousPostCodeLookupId(this.index))
          .flatMap(_.remove(PreviousAddressId(this.index)))
          .flatMap(_.remove(PreviousAddressListId(this.index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object AddressYearsId {
  override lazy val toString: String = "addressYears"

  implicit def cya: CheckYourAnswers[AddressYearsId] =
    new CheckYourAnswersIndividual[AddressYearsId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__addressYears"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_addressYears"))
      }
      override def row(id: AddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        AddressYearsCYA(
          label,
          hiddenLabel
        )().row(id)(changeUrl, ua)
      }

      override def updateRow(id: AddressYearsId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Nil
        }
    }
}
