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

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyPayeVariationsId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ CompanyPayeVariationsId.toString
}

object CompanyPayeVariationsId {
  override def toString: String = "companyPaye"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyPayeVariationsId] = {
    new CheckYourAnswers[CompanyPayeVariationsId] {

      def companyName(index: Int, userAnswers:UserAnswers) =
        userAnswers.get(CompanyDetailsId(index)) match {
          case Some(companyDetails) => companyDetails.companyName
          case _ => messages("messages__theCompany")
        }

      private val payeLabel = "messages__common__cya__paye"
      private def hiddenLabelPaye(index:Int, ua:UserAnswers) = messages("messages__visuallyhidden__dynamic_paye", companyName(index, ua))

      override def row(id: CompanyPayeVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[CompanyPayeVariationsId](payeLabel, hiddenLabelPaye(id.index, userAnswers))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyPayeVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[CompanyPayeVariationsId](payeLabel, hiddenLabelPaye(id.index, userAnswers))().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[CompanyPayeVariationsId](payeLabel, hiddenLabelPaye(id.index, userAnswers))().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}


