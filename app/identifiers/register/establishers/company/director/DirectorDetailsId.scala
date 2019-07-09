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

package identifiers.register.establishers.company.director

import identifiers._
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.OtherDirectorsId
import models.Link
import models.person.PersonDetails
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult, Reads}
import utils.checkyouranswers.CheckYourAnswers
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class DirectorDetailsId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[PersonDetails] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorDetailsId.toString

  override def cleanup(value: Option[PersonDetails], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.allDirectorsAfterDelete(this.establisherIndex).lengthCompare(10) match {
      case lengthCompare if lengthCompare <= 0 => userAnswers.remove(OtherDirectorsId(this.establisherIndex))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object DirectorDetailsId {
  def collectionPath(establisherIndex: Int): JsPath = EstablishersId(establisherIndex).path \ "director" \\ DirectorDetailsId.toString

  override def toString: String = "directorDetails"

  implicit def personDetails(implicit rds: Reads[PersonDetails], messages: Messages): CheckYourAnswers[DirectorDetailsId] = {
    new CheckYourAnswers[DirectorDetailsId] {

      override def row(id: DirectorDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails => {
          Seq(
            AnswerRow(
              "messages__director__cya__name",
              Seq(personDetails.fullName),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__name", personDetails.fullName).resolve)))
            ),
            AnswerRow(
              messages("messages__director__cya__dob", personDetails.firstAndLastName),
              Seq(DateHelper.formatDate(personDetails.date)),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__dob", personDetails.fullName).resolve)))
            )
          )
        }}

      override def updateRow(id: DirectorDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails =>
              Seq(
                AnswerRow(
                  "messages__director__cya__name",
                  Seq(personDetails.fullName),
                  answerIsMessageKey = false,
                  None
                ),
                AnswerRow(
                  messages("messages__director__cya__dob", personDetails.firstAndLastName),
                  Seq(DateHelper.formatDate(personDetails.date)),
                  answerIsMessageKey = false,
                  None
                )
              )
            }
        }
    }
  }
}
