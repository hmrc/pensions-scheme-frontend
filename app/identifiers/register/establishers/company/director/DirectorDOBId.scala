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
import models.Link
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Reads}
import utils.checkyouranswers.CheckYourAnswers
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class DirectorDOBId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ "directorDetails" \ DirectorDOBId.toString
}

object DirectorDOBId {
  override def toString: String = "date"

  implicit def personDetails(implicit rds: Reads[LocalDate], messages: Messages, answers: UserAnswers): CheckYourAnswers[DirectorDOBId] = {
    new CheckYourAnswers[DirectorDOBId] {

      def label(establisherIndex: Int, directorIndex: Int) =
        answers.get(DirectorNameId(establisherIndex, directorIndex)) match {
          case Some(name) => messages("messages__director__cya__dob", name.fullName)
          case _ => messages("messages__visuallyhidden__cya__dob")
        }

      override def row(id: DirectorDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { dob => {
          Seq(
            AnswerRow(
              label(id.establisherIndex, id.directorIndex),
              Seq(DateHelper.formatDate(dob)),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__cya__dob"))))
            )
          )
        }}

      override def updateRow(id: DirectorDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { dob =>
              Seq(
                AnswerRow(
                  label(id.establisherIndex, id.directorIndex),
                  Seq(DateHelper.formatDate(dob)),
                  answerIsMessageKey = false,
                  None
                )
              )
            }
        }
    }
  }
}
