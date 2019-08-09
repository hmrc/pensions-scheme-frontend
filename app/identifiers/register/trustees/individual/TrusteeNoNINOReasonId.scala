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

package identifiers.register.trustees.individual

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeNoNINOReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeNoNINOReasonId.toString

}

object TrusteeNoNINOReasonId {
  override def toString: String = "noNINOReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[TrusteeNoNINOReasonId] = {

    def label(index: Int): Option[String] =
      userAnswers.get(TrusteeNameId(index)) match {
      case Some(name) => Some(messages("messages__noNinoReason__heading", name.fullName))
      case _ => Some(messages("messages__trusteeNoNinoReason__cya_fallback"))
    }

    def hiddenLabel = Some(messages("messages__visuallyhidden__trustee__nino_no"))

    new CheckYourAnswers[TrusteeNoNINOReasonId] {
      override def row(id: TrusteeNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.index), hiddenLabel)().row(id)(changeUrl, userAnswers)


      override def updateRow(id: TrusteeNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}

