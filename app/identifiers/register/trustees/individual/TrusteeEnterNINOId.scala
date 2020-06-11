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
import identifiers.register.trustees
import identifiers.register.trustees.TrusteesId
import models.ReferenceValue
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersTrusteeIndividual, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class TrusteeEnterNINOId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeEnterNINOId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(TrusteeNoNINOReasonId(this.index))
}

object TrusteeEnterNINOId {

  override lazy val toString: String = "trusteeNino"

  implicit def cya(implicit userAnswers: UserAnswers, countryOptions: CountryOptions): CheckYourAnswers[TrusteeEnterNINOId] = {

    new CheckYourAnswersTrusteeIndividual[TrusteeEnterNINOId] {
      def getLabel(index: Int, ua: UserAnswers): (Message, Message) = {
        (dynamicMessage(index, ua, "messages__enterNINO"),
          dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_national_insurance_number"))
      }

      override def row(id: TrusteeEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, userAnswers)
        ReferenceValueCYA[TrusteeEnterNINOId](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: TrusteeEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, userAnswers)
        userAnswers.get(trustees.IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[TrusteeEnterNINOId](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[TrusteeEnterNINOId](label, hiddenLabel)().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
