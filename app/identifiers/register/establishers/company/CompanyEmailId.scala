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
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyEmailId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyContactDetails" \ CompanyEmailId.toString
}

object CompanyEmailId {
  override def toString: String = "emailAddress"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions, userAnswers: UserAnswers): CheckYourAnswers[CompanyEmailId] = new
      CheckYourAnswersCompany[CompanyEmailId] {

    private def label(index : Int, ua : UserAnswers) : String =
      dynamicMessage(index, ua, "messages__common_email__heading")

    private def hiddenLabel(index:  Int, ua: UserAnswers) : String =
      dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_email")

    override def row(id: CompanyEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      StringCYA(Some(label(id.index, userAnswers)), Some(messages(hiddenLabel(id.index, userAnswers))))()
        .row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: CompanyEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)
  }
}


