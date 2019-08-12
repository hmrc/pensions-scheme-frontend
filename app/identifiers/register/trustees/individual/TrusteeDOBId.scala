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

package identifiers.register.trustees.individual

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.Link
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{DateHelper, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.{AnswerRow, Message}

case class TrusteeDOBId(index: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeDOBId.toString
}

object TrusteeDOBId {
  override def toString: String = "dateOfBirth"

  implicit def cya(implicit answers: UserAnswers, messages: Messages): CheckYourAnswers[TrusteeDOBId] = {
    new CheckYourAnswers[TrusteeDOBId] {

      def label(index: Int): String =
        answers.get(TrusteeNameId(index)) match {
          case Some(name) => messages("messages__trustee__cya__dob", name.fullName)
          case _ => messages("messages__trustee__cya__dob", messages("messages__theTrustee"))
        }

      override def row(id: TrusteeDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) {
          dob => {
            Seq(
              AnswerRow(
                label(id.index),
                Seq(DateHelper.formatDate(dob)),
                answerIsMessageKey = false,
                Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__trustee__dob"))))
              )
            )
          }
        }

      override def updateRow(id: TrusteeDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
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
