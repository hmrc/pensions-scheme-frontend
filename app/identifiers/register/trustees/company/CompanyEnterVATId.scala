/*
 * Copyright 2020 HM Revenue & Customs
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

package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyEnterVATId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ CompanyEnterVATId.toString
}

object CompanyEnterVATId {
  override def toString: String = "companyVat"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyEnterVATId] = {

    def companyName(index: Int, userAnswers: UserAnswers) =
      userAnswers.get(CompanyDetailsId(index)) match {
        case Some(companyDetails) => companyDetails.companyName
        case _ => messages("messages__theCompany")
      }

    new CheckYourAnswers[CompanyEnterVATId] {

      private val labelVat = "messages__common__cya__vat"

      def hiddenLabelVat(index: Int, userAnswers: UserAnswers) =
        messages("messages__visuallyhidden__dynamic_vat_number", companyName(index, userAnswers))

      override def row(id: CompanyEnterVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[CompanyEnterVATId](labelVat, hiddenLabelVat(id.index, userAnswers))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyEnterVATId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => ReferenceValueCYA[CompanyEnterVATId](labelVat, hiddenLabelVat(id.index, userAnswers))()
            .row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[CompanyEnterVATId](labelVat, hiddenLabelVat(id.index, userAnswers))()
              .updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}



