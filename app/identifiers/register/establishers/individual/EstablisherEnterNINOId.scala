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

package identifiers.register.establishers.individual

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class EstablisherEnterNINOId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherEnterNINOId.toString
}

object EstablisherEnterNINOId {

  override lazy val toString: String = "establisherNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[EstablisherEnterNINOId] = {

    new CheckYourAnswers[EstablisherEnterNINOId] {
      def establisherName(index: Int): String = userAnswers.get(EstablisherNameId(index)).fold(messages("messages__theIndividual"))(_.fullName)
      def label(index: Int): String = messages("messages__enterNINO", establisherName(index))
      def hiddenLabel(index: Int): String = messages("messages__visuallyhidden__dynamic_national_insurance_number", establisherName(index))

      override def row(id: EstablisherEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[EstablisherEnterNINOId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: EstablisherEnterNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[EstablisherEnterNINOId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[EstablisherEnterNINOId](label(id.index), hiddenLabel(id.index))().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}