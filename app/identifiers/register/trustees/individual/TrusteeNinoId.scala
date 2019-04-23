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
import models.{Link, Nino}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, NinoCYA}
import viewmodels.AnswerRow

case class TrusteeNinoId(index: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeNinoId.toString
}

object TrusteeNinoId {
  override def toString: String = "trusteeNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[TrusteeNinoId] = {
    new CheckYourAnswers[TrusteeNinoId] {
      override def row(id: TrusteeNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        NinoCYA()().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: TrusteeNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(Nino.Yes(nino)) =>  userAnswers.get(IsTrusteeNewId(id.index)) match {
            case Some(true) => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some("messages__visuallyhidden__trustee__nino_yes_no")))))
            case _  => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false, None))
          }
          case Some(Nino.No(_)) => Seq(AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", changeUrl, Some(s"messages__visuallyhidden__trustee__nino_add")))))
          case _ => Seq.empty[AnswerRow]
        }
      }
    }
  }
}
