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
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class TrusteeHasNINOId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeHasNINOId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) => userAnswers.remove(TrusteeNoNINOReasonId(index))
      case Some(false) => userAnswers.remove(TrusteeEnterNINOId(index))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeHasNINOId {
  override def toString: String = "hasNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[TrusteeHasNINOId] = {

    def trusteeName(index: Int) = userAnswers.get(TrusteeNameId(index))
      .fold(messages("messages__theTrustee"))(_.fullName)

    def label(index: Int): Option[String] = Some(messages("messages__hasNINO", trusteeName(index)))

    def hiddenLabel(index: Int) =
      Some(messages("messages__visuallyhidden__dynamic_hasNino", trusteeName(index)))

    new CheckYourAnswers[TrusteeHasNINOId] {
      override def row(id: TrusteeHasNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeHasNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
