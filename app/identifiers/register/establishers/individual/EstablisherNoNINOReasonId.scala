/*
 * Copyright 2020 HM Revenue & Customs
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

package identifiers.register.establishers.individual

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class EstablisherNoNINOReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherNoNINOReasonId.toString
}

object EstablisherNoNINOReasonId {
  override def toString: String = "noNinoReason"

  implicit def cya(implicit ua: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[EstablisherNoNINOReasonId] = {

    def establisherName(index: Int) =
      ua.get(EstablisherNameId(index)).fold(messages("messages__thePerson"))(_.fullName)

    def label(index: Int) =
      Some(messages("messages__whyNoNINO", establisherName(index)))

    def hiddenLabel(index: Int) =
      Some(messages("messages__visuallyhidden__dynamic_noNinoReason", establisherName(index)))

    new CheckYourAnswers[EstablisherNoNINOReasonId] {
      override def row(id: EstablisherNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)


      override def updateRow(id: EstablisherNoNINOReasonId)(changeUrl: String,
                                                            userAnswers: UserAnswers
                                                           ): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
