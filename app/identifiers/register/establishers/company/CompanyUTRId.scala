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
import models.UniqueTaxReference
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class CompanyUTRId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyUniqueTaxReference" \ CompanyUTRId.toString
}

object CompanyUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[CompanyUTRId] = {

    def label(index: Int) = userAnswers.get(CompanyDetailsId(index)) match {
      case Some(name) => Some(messages("messages__companyUtr__heading", name))
      case _ => Some(messages("messages__companyUtr__title"))
    }

    def hiddenLabel(index: Int) = userAnswers.get(CompanyDetailsId(index)) match {
      case Some(name) => Some(messages("messages__companyUtr__heading", name))
      case _ => Some(messages("messages__companyUtr__title"))
    }

    new CheckYourAnswers[CompanyUTRId] {
      override def row(id: CompanyUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)


      override def updateRow(id: CompanyUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => StringCYA(label(id.index), hiddenLabel(id.index), true)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }

}


