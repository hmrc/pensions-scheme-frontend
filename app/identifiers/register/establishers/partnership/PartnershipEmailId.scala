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

package identifiers.register.establishers.partnership

import identifiers._
import identifiers.register.establishers.EstablishersId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class PartnershipEmailId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "partnershipContactDetails" \ PartnershipEmailId.toString
}

object PartnershipEmailId {
  override def toString: String = "emailAddress"

  implicit def cya(implicit messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[PartnershipEmailId] =
    new CheckYourAnswers[PartnershipEmailId] {

    override def row(id: PartnershipEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      val establisherName: String =
        userAnswers.get(PartnershipDetailsId(id.index)).fold(messages("messages__thePartnership"))(_.name)
      val label = messages("messages__enterEmail", establisherName)
      val hiddenLabel = Some(messages("messages__visuallyhidden__dynamic_email_address", establisherName))

      StringCYA(
        Some(label),
        hiddenLabel
      )().row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: PartnershipEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
      row(id)(changeUrl, userAnswers)
  }
}









