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
import models.Nino
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, NinoCYA}
import viewmodels.AnswerRow

case class DirectorNinoId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorNinoId.toString
}

object DirectorNinoId {
  override lazy val toString: String = "directorNino"

  implicit def nino(implicit messages: Messages): CheckYourAnswers[DirectorNinoId] = {

    val directorNinoIdCYA = (establisherIndex: Int, directorIndex: Int, ua: UserAnswers) => {
      val (label, reasonLabel) = ua.get(DirectorNameId(establisherIndex, directorIndex)) match {
        case Some(name) =>
          (messages("messages__director__cya__nino", name.fullName), messages("messages__director__cya__nino_reason", name.fullName))

        case None =>
          ("messages__director__cya__nino__fallback", "messages__director__cya__nino_reason__fallback")
      }

      NinoCYA[DirectorNinoId](
        label = label,
        reasonLabel = reasonLabel,
        changeHasNino = "messages__visuallyhidden__director__nino_yes_no",
        changeNino = "messages__visuallyhidden__director__nino",
        changeNoNino = "messages__visuallyhidden__director__nino_no"
      )()
    }

    new CheckYourAnswers[DirectorNinoId] {

      override def row(id: DirectorNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        directorNinoIdCYA(id.establisherIndex, id.directorIndex, userAnswers).row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => directorNinoIdCYA(id.establisherIndex, id.directorIndex, userAnswers).row(id)(changeUrl, userAnswers)
          case _ => directorNinoIdCYA(id.establisherIndex, id.directorIndex, userAnswers).updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
