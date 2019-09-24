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
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersDirectors, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class DirectorUTRId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(DirectorNoUTRReasonId(establisherIndex, directorIndex))
}

object DirectorUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[DirectorUTRId] = {

    new CheckYourAnswersDirectors[DirectorUTRId] {

      private def label(establisherIndex: Int, directorIndex: Int, ua:UserAnswers):String =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__utr__checkyouranswerslabel")

      private def hiddenLabel(establisherIndex: Int, directorIndex: Int, ua:UserAnswers):String =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__visuallyhidden__dynamic_utr")

      override def row(id: DirectorUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
          hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)


      override def updateRow(id: DirectorUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => ReferenceValueCYA(label(id.establisherIndex, id.directorIndex, userAnswers),
            hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers))()
            .updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}


