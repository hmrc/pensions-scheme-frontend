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
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeNewNinoId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeNewNinoId.toString
}

object TrusteeNewNinoId {

  override lazy val toString: String = "trusteeNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[TrusteeNewNinoId] = {

    new CheckYourAnswers[TrusteeNewNinoId] {

      private val label = "messages__common__nino"
      private val hiddenLabel = "messages__visuallyhidden__trustee__nino"

      override def row(id: TrusteeNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[TrusteeNewNinoId](label, hiddenLabel)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(trustees.IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[TrusteeNewNinoId](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[TrusteeNewNinoId](label, hiddenLabel)().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
