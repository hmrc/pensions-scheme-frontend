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
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class DirectorNewNinoId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorNewNinoId.toString
}

object DirectorNewNinoId {

  override lazy val toString: String = "directorNino"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[DirectorNewNinoId] = {

    new CheckYourAnswers[DirectorNewNinoId] {

      private val getLabel = (establisherIndex: Int, directorIndex: Int, ua: UserAnswers) =>
        ua.get(DirectorDetailsId(establisherIndex, directorIndex)) match {
          case Some(name) => messages("messages__director__cya__nino", name.firstAndLastName)
          case None   => "messages__common__nino"
        }

      private val hiddenLabel = "messages__visuallyhidden__director__nino"

      override def row(id: DirectorNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        ReferenceValueCYA[DirectorNewNinoId](getLabel(id.establisherIndex, id.directorIndex, userAnswers), hiddenLabel)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: DirectorNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val label = getLabel(id.establisherIndex, id.directorIndex, userAnswers)

        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) =>
            ReferenceValueCYA[DirectorNewNinoId](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[DirectorNewNinoId](label, hiddenLabel)().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}


