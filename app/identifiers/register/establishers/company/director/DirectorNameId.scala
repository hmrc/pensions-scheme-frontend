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
import models.Link
import models.person.PersonName
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.{AnswerRow, Message}

case class DirectorNameId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[PersonName] {
  override def path: JsPath =
    EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorNameId.toString
}

object DirectorNameId {
  def collectionPath(establisherIndex: Int): JsPath =
    EstablishersId(establisherIndex).path \ "director" \\ DirectorNameId.toString

  override def toString: String = "directorDetails"

  implicit def cya: CheckYourAnswers[DirectorNameId] = {
    new CheckYourAnswers[DirectorNameId] {

      override def row(id: DirectorNameId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails => {
          Seq(
            AnswerRow(
              Message("messages__directorName__cya"),
              Seq(personDetails.fullName),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl,
                Some(Message("messages__visuallyhidden__directorName", personDetails.fullName))))
            )
          )
        }}

      override def updateRow(id: DirectorNameId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails =>
              Seq(
                AnswerRow(
                  Message("messages__directorName__cya"),
                  Seq(personDetails.fullName),
                  answerIsMessageKey = false,
                  None
                )
              )
            }
        }
    }
  }
}
