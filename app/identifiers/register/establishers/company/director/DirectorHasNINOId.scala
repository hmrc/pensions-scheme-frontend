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
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class DirectorHasNINOId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorHasNINOId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) =>
        userAnswers.remove(DirectorNoNINOReasonId(establisherIndex, directorIndex))
      case Some(false) =>
        userAnswers.remove(DirectorEnterNINOId(establisherIndex, directorIndex))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}


object DirectorHasNINOId {
  override def toString: String = "hasNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[DirectorHasNINOId] = {

    def label(establisherIndex: Int, directorIndex: Int) =
      userAnswers.get(DirectorNameId(establisherIndex, directorIndex)) match {
        case Some(directorName) => Some(messages("messages__director__cya__nino", directorName.fullName))
        case _ => Some(messages("messages__director__cya__nino__fallback"))
      }

    def hiddenLabel = Some(messages("messages__visuallyhidden__director__nino_yes_no"))

    new CheckYourAnswers[DirectorHasNINOId] {
      override def row(id: DirectorHasNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(label(id.establisherIndex, id.directorIndex), hiddenLabel)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorHasNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => BooleanCYA(label(id.establisherIndex, id.directorIndex), hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
