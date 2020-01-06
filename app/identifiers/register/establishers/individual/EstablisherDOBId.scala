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
import models.Link
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{DateHelper, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.AnswerRow

case class EstablisherDOBId(index: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherDOBId.toString
}

object EstablisherDOBId {
  override def toString: String = "dateOfBirth"

  implicit def cya(implicit answers: UserAnswers, messages: Messages): CheckYourAnswers[EstablisherDOBId] = {
    new CheckYourAnswers[EstablisherDOBId] {

      def establisherName(index: Int): String = answers.get(EstablisherNameId(index)).fold(messages("messages__thePerson"))(_.fullName)
      def label(index: Int): String = messages("messages__DOB__heading", establisherName(index))
      def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_date_of_birth", establisherName(index)))

      override def row(id: EstablisherDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) {
          dob => {
            Seq(
              AnswerRow(
                label(id.index),
                Seq(DateHelper.formatDate(dob)),
                answerIsMessageKey = false,
                Some(Link("site.change", changeUrl, hiddenLabel(id.index)))
              )
            )
          }
        }

      override def updateRow(id: EstablisherDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { dob =>
              Seq(
                AnswerRow(
                  label(id.index),
                  Seq(DateHelper.formatDate(dob)),
                  answerIsMessageKey = false,
                  None
                )
              )
            }
        }
    }
  }
}
