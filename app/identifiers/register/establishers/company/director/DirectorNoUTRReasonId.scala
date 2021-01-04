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
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersDirectors}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class DirectorNoUTRReasonId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[String] {
  override def path: JsPath =
    EstablishersId(establisherIndex)
      .path \ "director" \ directorIndex \ DirectorNoUTRReasonId.toString
}

object DirectorNoUTRReasonId {
  override def toString: String = "noUtrReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   countryOptions: CountryOptions): CheckYourAnswers[DirectorNoUTRReasonId] = {

    new CheckYourAnswersDirectors[DirectorNoUTRReasonId] {

      private def label(establisherIndex: Int, directorIndex: Int, ua:UserAnswers): Message =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__whyNoUTR")

      private def hiddenLabel(establisherIndex: Int, directorIndex: Int, ua:UserAnswers): Message =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__visuallyhidden__dynamic_noUtrReason")

      override def row(id: DirectorNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(id.establisherIndex, id.directorIndex, userAnswers)),
          Some(hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers)))()
          .row(id)(changeUrl, userAnswers)


      override def updateRow(id: DirectorNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
