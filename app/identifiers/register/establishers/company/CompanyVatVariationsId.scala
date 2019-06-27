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
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers._
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyVatVariationsId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyVat" \ CompanyVatVariationsId.toString
}

object CompanyVatVariationsId {
  override def toString: String = "vat"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[CompanyVatVariationsId] = {

    def label(index: Int) = userAnswers.get(CompanyDetailsId(index)) match {
      case Some(name) => messages("messages__vatVariations__heading", name)
      case _ => messages("messages__vatVariations__company_title")
    }

    def hiddenLabel(index: Int) = userAnswers.get(CompanyDetailsId(index)) match {
      case Some(name) => messages("messages__vatVariations__heading", name)
      case _ => messages("messages__vatVariations__company_title")
    }

    new CheckYourAnswers[CompanyVatVariationsId] {
      override def row(id: CompanyVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(id.index)), Some(hiddenLabel(id.index)))().row(id)(changeUrl, userAnswers)


      override def updateRow(id: CompanyVatVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        (userAnswers.get(IsEstablisherNewId(id.index)), userAnswers.get(id)) match {
          case (Some(true), _) => row(id)(changeUrl, userAnswers)
          case (_, Some(_)) => StringCYA(Some(label(id.index)), Some(hiddenLabel(id.index)))().updateRow(id)(changeUrl, userAnswers)
          case _ => addLink(label(id.index), changeUrl, Some(s"${hiddenLabel(id.index)}_add"))
        }
    }
  }
}





