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

package identifiers.register.establishers.individual

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.{Link, Nino}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, NinoCYA}
import viewmodels.AnswerRow


case class EstablisherNinoId(index: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherNinoId.toString
}

object EstablisherNinoId {

  override lazy val toString: String = "establisherNino"
  val label = "messages__establisher_individual_nino_question_cya_label"
  val changeHasNino = "messages__visuallyhidden__establisher__nino_yes_no"
  val changeNino = "messages__visuallyhidden__establisher__nino"
  val changeNoNino = "messages__visuallyhidden__establisher__nino_no"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[EstablisherNinoId] = {
    new CheckYourAnswers[EstablisherNinoId] {
      override def row(id: EstablisherNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        NinoCYA(label, changeHasNino, changeNino, changeNoNino)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: EstablisherNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(Nino.Yes(nino)) =>  userAnswers.get(IsEstablisherNewId(id.index)) match {
            case Some(true) => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeHasNino)))))
            case _  => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false, None))
          }
          case Some(Nino.No(_)) => Seq(AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", changeUrl, Some(s"messages__visuallyhidden__establisher__nino_add")))))
          case _ => Seq.empty[AnswerRow]
        }
      }
    }
  }
}
