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

package identifiers.register.establishers.individual

import java.time.LocalDate

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.Link
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersIndividual}
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class EstablisherDOBId(index: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherDOBId.toString
}

object EstablisherDOBId {
  override def toString: String = "dateOfBirth"

  implicit def cya(implicit answers: UserAnswers): CheckYourAnswers[EstablisherDOBId] = {
    new CheckYourAnswersIndividual[EstablisherDOBId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__DOB__heading"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_date_of_birth"))
      }

      override def row(id: EstablisherDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
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

      override def updateRow(id: EstablisherDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val (label, _) = getLabel(id.index, userAnswers)
        userAnswers.get(IsEstablisherNewId(id.index)) match {
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
