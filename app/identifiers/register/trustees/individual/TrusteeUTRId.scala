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
import identifiers.register.trustees
import identifiers.register.trustees.TrusteesId
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(TrusteeNoUTRReasonId(index))
}

object TrusteeUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[TrusteeUTRId] = {

    def trusteeName(index: Int) = userAnswers.get(TrusteeNameId(index)).fold(messages("messages__theTrustee"))(_.fullName)
    def label(index: Int): String = messages("messages__enterUTR", trusteeName(index))
    def hiddenLabel(index: Int) = messages("messages__visuallyhidden__dynamic_utr", trusteeName(index))

    new CheckYourAnswers[TrusteeUTRId] {
      override def row(id: TrusteeUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[TrusteeUTRId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(trustees.IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[TrusteeUTRId](label(id.index), hiddenLabel(id.index))().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
