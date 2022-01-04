/*
 * Copyright 2022 HM Revenue & Customs
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

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.AddressYears
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersDirectors}
import viewmodels.{AnswerRow, Message}

case class DirectorAddressYearsId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[AddressYears] {

  override def path: JsPath =
    EstablishersId(establisherIndex)
      .path \ "director" \ directorIndex \ DirectorAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers
          .remove(DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex))
          .flatMap(_.remove(DirectorPreviousAddressId(establisherIndex, directorIndex)))
          .flatMap(_.remove(DirectorPreviousAddressListId(establisherIndex, directorIndex)))
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object DirectorAddressYearsId {
  override lazy val toString: String = "companyDirectorAddressYears"

  implicit def cya: CheckYourAnswers[DirectorAddressYearsId] = {

    new CheckYourAnswersDirectors[DirectorAddressYearsId] {

      private def label(establisherIndex: Int, directorIndex: Int, ua: UserAnswers): Message =
          dynamicMessage(establisherIndex, directorIndex, ua, "messages__director__cya__address_years")


      private def hiddenLabel(establisherIndex: Int, directorIndex: Int, ua: UserAnswers): Message =
          dynamicMessage(establisherIndex, directorIndex, ua, "messages__visuallyhidden__dynamic_addressYears")

      override def row(id: DirectorAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
          hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) =>
            AddressYearsCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
              hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers))()
              .row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
              hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers))()
              .updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
