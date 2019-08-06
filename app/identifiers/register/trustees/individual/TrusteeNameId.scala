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

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.Link
import models.person.PersonName
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.{AnswerRow, Message}

case class TrusteeNameId(trusteeIndex: Int) extends TypedIdentifier[PersonName] {
  override def path: JsPath = TrusteesId(trusteeIndex).path \ TrusteeNameId.toString
}

object TrusteeNameId {
  override def toString: String = "trusteeName"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[TrusteeNameId] = {
    new CheckYourAnswers[TrusteeNameId] {
      override def row(id: TrusteeNameId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails =>
          Seq(AnswerRow(
            "messages__trusteeName__cya",
            Seq(personDetails.fullName),
            answerIsMessageKey = false,
            Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__trusteeName", personDetails.fullName).resolve)))
          ))
        }

      override def updateRow(id: TrusteeNameId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.trusteeIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails =>
              Seq(AnswerRow(
                "messages__trusteeName__cya",
                Seq(personDetails.fullName),
                answerIsMessageKey = false,
                None
              ))
            }
        }
    }
  }
}
