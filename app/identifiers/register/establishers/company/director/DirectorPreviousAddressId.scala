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

package identifiers.register.establishers.company.director

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.libs.json._
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, CheckYourAnswersDirectors, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class DirectorPreviousAddressId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[Address] {
  override def path: JsPath =
    EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorPreviousAddressId.toString
}

object DirectorPreviousAddressId {
  override def toString: String = "previousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[DirectorPreviousAddressId] = {

    new CheckYourAnswersDirectors[DirectorPreviousAddressId] {

      private def label(establisherIndex: Int, directorIndex: Int, ua: UserAnswers): Message =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__previousAddressFor")

      private def hiddenLabel(establisherIndex: Int, directorIndex: Int, ua: UserAnswers): Message =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__visuallyhidden__dynamic_previousAddress")

      override def row(id: DirectorPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
          hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers)
      : Seq[AnswerRow] =
        PreviousAddressCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
          hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers),
          userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)),
          userAnswers.get(DirectorConfirmPreviousAddressId(id.establisherIndex, id.directorIndex))
        )().updateRow(id)(changeUrl, userAnswers)
    }
  }
}
