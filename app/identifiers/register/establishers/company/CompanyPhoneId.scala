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

package identifiers.register.establishers.company

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany}
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class CompanyPhoneId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyContactDetails" \ CompanyPhoneId.toString
}

object CompanyPhoneId {
  override def toString: String = "phoneNumber"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions, userAnswers: UserAnswers)
  : CheckYourAnswers[CompanyPhoneId] = new
      CheckYourAnswersCompany[CompanyPhoneId] {

    private def hiddenLabel(index: Int, ua: UserAnswers): String =
      dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_phone_number")

    override def row(id: CompanyPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      val companyName = userAnswers.get(CompanyDetailsId(id.index)).fold(messages("messages__theCompany"))(_
        .companyName)
      val label = "messages__enterPhoneNumber"

      StringCYA(Some(messages(label, companyName)), Some(hiddenLabel(id.index, userAnswers)))()
        .row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: CompanyPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)
    (changeUrl, userAnswers)
  }
}




