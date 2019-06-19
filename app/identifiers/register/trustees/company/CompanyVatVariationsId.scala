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

package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.Reference
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyVatVariationsId(index: Int) extends TypedIdentifier[Reference] {
  override def path: JsPath = TrusteesId(index).path \ CompanyVatVariationsId.toString
}

object CompanyVatVariationsId {
  override def toString: String = "companyVat"

  val labelVat = "messages__common__cya__vat"
  val hiddenLabelVat = "messages__visuallyhidden__trustee__vat_number"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyVatVariationsId] = {
    new CheckYourAnswers[CompanyVatVariationsId] {

      override def row(id: CompanyVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceCYA[CompanyVatVariationsId]("messages__common__cya__vat", hiddenLabelVat)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => ReferenceCYA[CompanyVatVariationsId]("messages__common__cya__vat", hiddenLabelVat)().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceCYA[CompanyVatVariationsId](labelVat, hiddenLabelVat)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}



