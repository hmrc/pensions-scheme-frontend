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
import identifiers.register.trustees.partnership.PartnershipDetailsId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class TrusteeHasUTRId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeHasUTRId.toString
  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) =>
        userAnswers.remove(TrusteeNoUTRReasonId(index))
      case Some(false) =>
        userAnswers.remove(TrusteeUTRId(index))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeHasUTRId {
  override def toString: String = "hasUtr"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[TrusteeHasUTRId] = {

    def trusteeName(index: Int) = userAnswers.get(TrusteeNameId(index)).fold(messages("messages__theTrustee"))(_.fullName)
    def label(index: Int) = Some(messages("messages__hasUtr__h1", trusteeName(index)))
    def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_hasUtr", trusteeName(index)))

    new CheckYourAnswers[TrusteeHasUTRId] {
      override def row(id: TrusteeHasUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeHasUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _          => Seq.empty[AnswerRow]
        }
    }
  }
}
