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
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import viewmodels.AnswerRow

case class DirectorEnterNINOId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorEnterNINOId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(DirectorNoNINOReasonId(this.establisherIndex, this.directorIndex))
}

object DirectorEnterNINOId {

  override lazy val toString: String = "directorNino"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[DirectorEnterNINOId] = {

    new CheckYourAnswers[DirectorEnterNINOId] {

      private val hiddenLabel = "messages__visuallyhidden__director__nino"
      private val label = "messages__common__nino"

      override def row(id: DirectorEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[DirectorEnterNINOId](label, hiddenLabel)()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) =>
            ReferenceValueCYA[DirectorEnterNINOId](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[DirectorEnterNINOId](label, hiddenLabel)().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}


