/*
 * Copyright 2022 HM Revenue & Customs
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

package identifiers.register.establishers.partnership.partner

import identifiers._
import identifiers.register.establishers.EstablishersId
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartners}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class PartnerEmailId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \
    "partnerContactDetails" \ PartnerEmailId.toString

}

object PartnerEmailId {
  override def toString: String = "emailAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnerEmailId] = {

    new CheckYourAnswersPartners[PartnerEmailId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers): Message =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__enterEmail")

      private def hiddenLabel(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers): Message =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_email_address")

      override def row(id: PartnerEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(id.establisherIndex, id.partnerIndex, userAnswers)),
          Some(hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers)))()
          .row(id)(changeUrl, userAnswers)


      override def updateRow(id: PartnerEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        row(id)(changeUrl, userAnswers)
    }
  }
}


