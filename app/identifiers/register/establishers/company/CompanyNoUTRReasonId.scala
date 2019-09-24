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
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyNoUTRReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ CompanyNoUTRReasonId.toString
}

object CompanyNoUTRReasonId {
  override def toString: String = "noUtrReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[CompanyNoUTRReasonId] = {

    def label(index: Int) = userAnswers.get(CompanyDetailsId(index)) match {
      case Some(details) => Some(messages("messages__noCompanyUtr__heading", details.companyName))
      case _ => Some(messages("messages__noCompanyUtr__title"))
    }

    def hiddenLabel = Some(messages("messages__visuallyhidden__noCompanyUTRReason"))

    new CheckYourAnswers[CompanyNoUTRReasonId] {
      override def row(id: CompanyNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.index), hiddenLabel)().row(id)(changeUrl, userAnswers)


      override def updateRow(id: CompanyNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => StringCYA(label(id.index), hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}




