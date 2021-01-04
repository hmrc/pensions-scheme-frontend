/*
 * Copyright 2021 HM Revenue & Customs
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

package identifiers.register.establishers.partnership

import identifiers._
import identifiers.register.establishers.EstablishersId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartnership}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class PartnershipEmailId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "partnershipContactDetails" \ PartnershipEmailId.toString
}

object PartnershipEmailId {
  override def toString: String = "emailAddress"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnershipEmailId] =
    new CheckYourAnswersPartnership[PartnershipEmailId] {
      private def label(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__enterEmail")

      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_email_address")

      override def row(id: PartnershipEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        StringCYA(
          Some(label(id.index, userAnswers)),
          Some(hiddenLabel(id.index, userAnswers))
        )().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: PartnershipEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)
    }
}









