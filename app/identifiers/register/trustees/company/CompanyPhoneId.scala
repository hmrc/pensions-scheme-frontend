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
import identifiers.register.trustees.TrusteesId
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersTrusteeCompany}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class CompanyPhoneId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ "companyContactDetails" \ CompanyPhoneId.toString
}

object CompanyPhoneId {
  override def toString: String = "phoneNumber"

  implicit def cya(implicit countryOptions: CountryOptions, userAnswers: UserAnswers): CheckYourAnswers[CompanyPhoneId] = new
      CheckYourAnswersTrusteeCompany[CompanyPhoneId] {
    def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
      (dynamicMessage(index, ua, "messages__enterPhoneNumber"),
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_phone_number"))
    }

    override def row(id: CompanyPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      val (label, hiddenLabel) = getLabel(id.index, userAnswers)
      StringCYA(
        Some(label),
        Some(hiddenLabel)
      )().row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: CompanyPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
      row(id)(changeUrl, userAnswers)
  }

}
