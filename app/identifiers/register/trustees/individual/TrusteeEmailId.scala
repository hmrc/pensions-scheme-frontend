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

package identifiers.register.trustees.individual

import identifiers._
import identifiers.register.trustees.TrusteesId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeEmailId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ "trusteeContactDetails" \ TrusteeEmailId.toString
}

object TrusteeEmailId {
  override def toString: String = "emailAddress"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions, userAnswers: UserAnswers)
  : CheckYourAnswers[TrusteeEmailId] = new
      CheckYourAnswers[TrusteeEmailId] {

    override def row(id: TrusteeEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
      def trusteeName(index: Int): String = userAnswers.get(TrusteeNameId(index)).fold(messages
      ("messages__theIndividual"))(_.fullName)

      def label(index: Int): String = messages("messages__enterEmail", trusteeName(index))

      def hiddenLabel(index: Int): Option[String] = Some(messages("messages__visuallyhidden__dynamic_email_address",
        trusteeName(index)))

      StringCYA(
        Some(label(id.index)),
        hiddenLabel(id.index)
      )().row(id)(changeUrl, userAnswers)
    }

    override def updateRow(id: TrusteeEmailId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)
    (changeUrl, userAnswers)
  }
}
