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

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class EstablisherHasUTRId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherHasUTRId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) =>
        userAnswers.remove(EstablisherNoUTRReasonId(index))
      case Some(false) =>
        userAnswers.remove(EstablisherUTRId(index))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object EstablisherHasUTRId {
  override def toString: String = "hasUtr"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[EstablisherHasUTRId] = {

    def establisherName(index: Int) = userAnswers.get(EstablisherNameId(index)).fold(messages
    ("messages__theIndividual"))(_.fullName)

    def label(index: Int) = Some(messages("messages__hasUTR", establisherName(index)))

    def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_hasUtr", establisherName(index)))

    new CheckYourAnswers[EstablisherHasUTRId] {
      override def row(id: EstablisherHasUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: EstablisherHasUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
