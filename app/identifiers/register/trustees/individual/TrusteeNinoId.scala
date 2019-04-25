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

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.Nino
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, NinoCYA}
import viewmodels.AnswerRow

case class TrusteeNinoId(index: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeNinoId.toString
}

object TrusteeNinoId {

  override def toString: String = "trusteeNino"

  val label = "messages__trusteeNino_question_cya_label"
  val changeHasNino = "messages__visuallyhidden__trustee__nino_yes_no"
  val changeNino = "messages__visuallyhidden__trustee__nino"
  val changeNoNino = "messages__visuallyhidden__trustee__nino_no"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[TrusteeNinoId] = {
    new CheckYourAnswers[TrusteeNinoId] {
      override def row(id: TrusteeNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        NinoCYA(label, changeHasNino, changeNino, changeNoNino)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        NinoCYA(label, changeHasNino, changeNino, changeNoNino, userAnswers.get(IsTrusteeNewId(id.index)))().updateRow(id)(changeUrl, userAnswers)
    }
  }
}
