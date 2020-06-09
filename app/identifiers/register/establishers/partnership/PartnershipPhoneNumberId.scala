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
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipPhoneNumberId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "partnershipContactDetails" \ PartnershipPhoneNumberId
    .toString
}

object PartnershipPhoneNumberId {
  override def toString: String = "phoneNumber"

  implicit def cya(implicit messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[PartnershipPhoneNumberId] =
    new CheckYourAnswers[PartnershipPhoneNumberId] {

    override def row(id: PartnershipPhoneNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      val establisherName: String =
        userAnswers.get(PartnershipDetailsId(id.index))
          .fold(messages("messages__thePartnership"))(_.name)
      val label = messages("messages__enterPhoneNumber", establisherName)
      val hiddenLabel = Some(messages("messages__visuallyhidden__dynamic_phone_number", establisherName))

      StringCYA(
        Some(label),
        hiddenLabel
      )().row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: PartnershipPhoneNumberId)(changeUrl: String,
                                                         userAnswers: UserAnswers): Seq[AnswerRow] =
      row(id)(changeUrl, userAnswers)
  }
}










