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
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyEnterVATId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ CompanyEnterVATId.toString
}

object CompanyEnterVATId {
  override def toString: String = "companyVat"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyEnterVATId] = {
    new CheckYourAnswers[CompanyEnterVATId] {

      private def companyName(index: Int,  ua:UserAnswers) =
        ua.get(CompanyDetailsId(index)) match {
          case Some(companyDetails) => companyDetails.companyName
          case _ => messages("messages__theCompany")
        }

      private def hiddenLabelVat(index:Int, ua:UserAnswers) =
        messages("messages__visuallyhidden__dynamic_vat", companyName(index, ua))

      private val vatLabel = "messages__common__cya__vat"

      override def row(id: CompanyEnterVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[CompanyEnterVATId](vatLabel, hiddenLabelVat(id.index, userAnswers))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyEnterVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[CompanyEnterVATId](vatLabel, hiddenLabelVat(id.index, userAnswers))().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[CompanyEnterVATId](vatLabel, hiddenLabelVat(id.index, userAnswers))().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}





