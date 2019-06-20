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
import identifiers.register.establishers.EstablishersId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class CompanyPhoneId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyContactDetails" \ CompanyPhoneId.toString
}

object CompanyPhoneId {
  override def toString: String = "phoneNumber"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions, userAnswers: UserAnswers): CheckYourAnswers[CompanyPhoneId] = new
      CheckYourAnswers[CompanyPhoneId] {
    private val hiddenLabel = Some(messages("messages__common_company_phone__visually_hidden_change_label"))

    override def row(id: CompanyPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      StringCYA[CompanyPhoneId](userAnswers.get(CompanyDetailsId(id.index)).map(companyDetails =>
        messages("messages__common_phone__cya_label", companyDetails.companyName)),
        hiddenLabel)().row(id)(changeUrl, userAnswers)
    }
    override def updateRow(id: CompanyPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      StringCYA[CompanyPhoneId](userAnswers.get(CompanyDetailsId(id.index)).map(companyDetails =>
        messages("messages__common_phone__cya_label", companyDetails.companyName)),
        hiddenLabel)().row(id)(changeUrl, userAnswers)
    }
  }
}




