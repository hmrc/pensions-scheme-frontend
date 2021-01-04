/*
 * Copyright 2021 HM Revenue & Customs
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
import models.ReferenceValue
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersDirectors, ReferenceValueCYA}
import viewmodels.{AnswerRow, Message}

case class DirectorEnterNINOId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath =
    EstablishersId(establisherIndex)
      .path \ "director" \ directorIndex \ DirectorEnterNINOId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(DirectorNoNINOReasonId(this.establisherIndex, this.directorIndex))
}

object DirectorEnterNINOId {

  override lazy val toString: String = "directorNino"

  implicit def cya: CheckYourAnswers[DirectorEnterNINOId] = {

    new CheckYourAnswersDirectors[DirectorEnterNINOId] {

      private def label(establisherIndex: Int, directorIndex: Int, ua:UserAnswers): Message =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__common__nino")

      private def hiddenText(establisherIndex: Int, directorIndex: Int, ua:UserAnswers): Message =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__visuallyhidden__dynamic_nino")

      override def row(id: DirectorEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[DirectorEnterNINOId](label(id.establisherIndex, id.directorIndex, userAnswers),
          hiddenText(id.establisherIndex, id.directorIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) =>
            ReferenceValueCYA[DirectorEnterNINOId](label(id.establisherIndex, id.directorIndex, userAnswers),
              hiddenText(id.establisherIndex, id.directorIndex, userAnswers))()
              .row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[DirectorEnterNINOId](label(id.establisherIndex, id.directorIndex, userAnswers),
              hiddenText(id.establisherIndex, id.directorIndex, userAnswers))()
              .updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}


