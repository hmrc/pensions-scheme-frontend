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
import models.{Link, UniqueTaxReference}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class UniqueTaxReferenceId(index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(index).path \ UniqueTaxReferenceId.toString
}

object UniqueTaxReferenceId {
  override def toString: String = "uniqueTaxReference"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[UniqueTaxReferenceId] = {
    new CheckYourAnswers[UniqueTaxReferenceId] {
      override def row(id: UniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        UniqueTaxReferenceCYA()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: UniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) =>
            userAnswers.get(IsEstablisherNewId(id.index)) match {
              case Some(true) => Seq(AnswerRow("messages__common__nino", Seq(utr), answerIsMessageKey = false,
                Some(Link("site.change", changeUrl, Some("messages__visuallyhidden__establisher__utr_yes_no")))))
              case _  => Seq(AnswerRow("messages__establisher_individual_utr_cya_label", Seq(utr), answerIsMessageKey = false, None))
            }
          case Some(UniqueTaxReference.No(_)) => Seq(AnswerRow("messages__establisher_individual_utr_cya_label", Seq("site.not_entered"), answerIsMessageKey = true, None))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
