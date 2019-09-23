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
import models.Vat
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.VatCYA
import viewmodels.AnswerRow

case class CompanyVatId(index: Int) extends TypedIdentifier[Vat] {
  override def path: JsPath = EstablishersId(index).path \ CompanyVatId.toString
}

object CompanyVatId {
  override def toString: String = "companyVat"

  val labelYesNo = "messages__company__cya__vat_yes_no"
  val hiddenLabelYesNo = "messages__visuallyhidden__establisher__vat_yes_no"
  val hiddenLabelVat = "messages__visuallyhidden__establisher__vat_number"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[CompanyVatId] = {
    new CheckYourAnswers[CompanyVatId] {

      override def row(id: CompanyVatId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        VatCYA(Some(labelYesNo), hiddenLabelYesNo, hiddenLabelVat)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyVatId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => VatCYA(Some(labelYesNo), hiddenLabelYesNo, hiddenLabelVat)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}




