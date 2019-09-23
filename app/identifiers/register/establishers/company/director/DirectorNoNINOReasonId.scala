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
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class DirectorNoNINOReasonId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorNoNINOReasonId.toString

}

object DirectorNoNINOReasonId {
  override def toString: String = "noNinoReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[DirectorNoNINOReasonId] = {

    def label(establisherIndex: Int, directorIndex: Int) = userAnswers.get(DirectorNameId(establisherIndex, directorIndex)) match {
      case Some(name) => Some(messages("messages__noGenericNino__heading", name.fullName))
      case _ => Some(messages("messages__directorNoNinoReason__cya_fallback"))
    }

    def hiddenLabel = Some(messages("messages__visuallyhidden__director__nino_no"))

    new CheckYourAnswers[DirectorNoNINOReasonId] {
      override def row(id: DirectorNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.establisherIndex, id.directorIndex), hiddenLabel)().row(id)(changeUrl, userAnswers)


      override def updateRow(id: DirectorNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}



