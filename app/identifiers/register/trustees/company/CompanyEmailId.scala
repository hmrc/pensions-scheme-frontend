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
import identifiers.register.trustees.TrusteesId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyEmailId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ "companyContactDetails" \ CompanyEmailId.toString
}

object CompanyEmailId {
  override def toString: String = "emailAddress"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions, userAnswers: UserAnswers): CheckYourAnswers[CompanyEmailId] = new
      CheckYourAnswers[CompanyEmailId] {

    override def row(id: CompanyEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      val trusteeName: String = userAnswers.get(CompanyDetailsId(id.index)).fold(messages("messages__theTrustee"))(_.companyName)
      val label = messages("messages__common_email__heading", trusteeName)
      val hiddenLabel = Some(messages("messages__visuallyhidden__dynamic_email", trusteeName))

      StringCYA(
        Some(label),
        hiddenLabel
      )().row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: CompanyEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)
  }

}
