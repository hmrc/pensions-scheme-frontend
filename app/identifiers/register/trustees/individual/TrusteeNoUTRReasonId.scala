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
import identifiers.register.trustees.partnership.PartnershipDetailsId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeNoUTRReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeNoUTRReasonId.toString
}

object TrusteeNoUTRReasonId {
  override def toString: String = "noUtrReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions
                  ): CheckYourAnswers[TrusteeNoUTRReasonId] = {

    def trusteeName(index: Int) = userAnswers.get(TrusteeNameId(index))
      .fold(messages("messages__theTrustee"))(_.fullName)

    def label(index: Int) = Some(messages("messages__whyNoUTR", trusteeName(index)))

    def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_noUtrReason", trusteeName(index)))

    new CheckYourAnswers[TrusteeNoUTRReasonId] {
      override def row(id: TrusteeNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }

}
