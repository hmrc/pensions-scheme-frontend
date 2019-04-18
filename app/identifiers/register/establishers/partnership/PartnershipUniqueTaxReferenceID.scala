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

package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import identifiers.register.establishers.company.CompanyUniqueTaxReferenceId
import models.{Link, UniqueTaxReference}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class PartnershipUniqueTaxReferenceID(index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipUniqueTaxReferenceID.toString
}

object PartnershipUniqueTaxReferenceID {
  override def toString: String = "partnershipUniqueTaxReference"

  val label = "messages__partnership__checkYourAnswers__utr"
  val utrLabel = "messages__visuallyhidden__partnership__utr_yes_no"
  val changeHasUtr = "messages__visuallyhidden__partnership__utr_yes_no"
  val changeUtr = "messages__visuallyhidden__partnership__utr"
  val changeNoUtr = "messages__visuallyhidden__partnership__utr_no"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[CompanyUniqueTaxReferenceId] = {

    new CheckYourAnswers[CompanyUniqueTaxReferenceId] {
      override def row(id: CompanyUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        UniqueTaxReferenceCYA()().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: CompanyUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) =>
            userAnswers.get(IsEstablisherNewId(id.index)) match {
              case Some(true) => Seq(
                AnswerRow(label, Seq(s"${UniqueTaxReference.Yes}"), answerIsMessageKey = false,
                  Some(Link("site.change", changeUrl, Some(changeHasUtr)))),
                AnswerRow(utrLabel, Seq(utr), answerIsMessageKey = false,
                  Some(Link("site.change", changeUrl, Some(changeUtr))))
              )
              case _  => Seq(AnswerRow(utrLabel, Seq(utr), answerIsMessageKey = false, None))
            }
          case Some(UniqueTaxReference.No(_)) => Seq(AnswerRow(utrLabel, Seq("site.not_entered"), answerIsMessageKey = true, None))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
