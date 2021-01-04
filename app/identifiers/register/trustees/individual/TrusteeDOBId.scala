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

package identifiers.register.trustees.individual

import java.time.LocalDate

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.Link
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersTrusteeIndividual}
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class TrusteeDOBId(index: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeDOBId.toString
}

object TrusteeDOBId {
  override def toString: String = "dateOfBirth"

  implicit def cya(implicit answers: UserAnswers): CheckYourAnswers[TrusteeDOBId] = {
    new CheckYourAnswersTrusteeIndividual[TrusteeDOBId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__DOB__heading"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_date_of_birth"))
      }

      override def row(id: TrusteeDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, userAnswers)
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) {
          dob => {
            Seq(
              AnswerRow(
                label,
                Seq(DateHelper.formatDate(dob)),
                answerIsMessageKey = false,
                Some(Link("site.change", changeUrl, Some(hiddenLabel)))
              )
            )
          }
        }
      }

      override def updateRow(id: TrusteeDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val (label, _) = getLabel(id.index, userAnswers)
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { dob =>
              Seq(
                AnswerRow(
                  label,
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
}
