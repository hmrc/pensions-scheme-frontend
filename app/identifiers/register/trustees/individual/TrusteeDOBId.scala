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

package identifiers.register.trustees.individual

import java.time.LocalDate

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.Link
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.{DateHelper, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeDOBId(index: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeDOBId.toString
}

object TrusteeDOBId {
  override def toString: String = "dateOfBirth"

  implicit def cya(implicit answers: UserAnswers, messages: Messages): CheckYourAnswers[TrusteeDOBId] = {
    new CheckYourAnswers[TrusteeDOBId] {

      def trusteeName(index: Int): String = answers.get(TrusteeNameId(index)).fold(messages("messages__theTrustee"))
      (_.fullName)

      def label(index: Int): String = messages("messages__DOB__heading", trusteeName(index))

      def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_date_of_birth", trusteeName
      (index)))

      override def row(id: TrusteeDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
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
