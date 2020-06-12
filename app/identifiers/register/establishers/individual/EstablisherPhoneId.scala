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

package identifiers.register.establishers.individual

import identifiers._
import identifiers.register.establishers.EstablishersId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class EstablisherPhoneId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "contactDetails" \ EstablisherPhoneId.toString
}

object EstablisherPhoneId {
  override def toString: String = "phoneNumber"

  implicit def cya(implicit messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[EstablisherPhoneId] =
    new CheckYourAnswers[EstablisherPhoneId] {

    override def row(id: EstablisherPhoneId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {

      val establisherName: String =
        ua.get(EstablisherNameId(id.index)).fold(messages("messages__theIndividual"))(_.fullName)

      val label: String = messages("messages__enterPhoneNumber", establisherName)

      val hiddenLabel: Option[String] =
        Some(messages("messages__visuallyhidden__dynamic_phone_number", establisherName))

      StringCYA(
        Some(label),
        hiddenLabel
      )().row(id)(changeUrl, ua)
    }

    override def updateRow(id: EstablisherPhoneId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
      row(id)(changeUrl, userAnswers)
  }
}
