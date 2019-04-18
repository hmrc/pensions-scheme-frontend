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

package identifiers.register.establishers.company

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.{Link, Paye, Vat}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Reads}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.{PayeCYA, VatCYA}
import viewmodels.AnswerRow

case class CompanyPayeId(index: Int) extends TypedIdentifier[Paye] {
  override def path: JsPath = EstablishersId(index).path \ CompanyPayeId.toString
}

object CompanyPayeId {
  override def toString: String = "companyPaye"

  val labelYesNo = "messages__company__cya__paye_yes_no"
  val hiddenLabelYesNo = "messages__visuallyhidden__establisher__paye_yes_no"
  val hiddenLabelVat = "messages__visuallyhidden__establisher__paye_number"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[CompanyPayeId] = {
    new CheckYourAnswers[CompanyPayeId] {

      override def row(id: CompanyPayeId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PayeCYA(Some(labelYesNo), hiddenLabelYesNo, hiddenLabelVat)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyPayeId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(Paye.Yes(paye)) => userAnswers.get(IsEstablisherNewId(id.index)) match {
            case Some(true) => Seq(AnswerRow(labelYesNo, Seq(paye), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(hiddenLabelYesNo)))))
            case _  => Seq(AnswerRow(labelYesNo, Seq(paye), answerIsMessageKey = false, None))
          }
          case Some(Paye.No) => Seq(AnswerRow(labelYesNo, Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", changeUrl, Some(s"${hiddenLabelVat}_add")))))

          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}


