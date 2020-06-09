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

package identifiers.register.establishers.individual

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import viewmodels.AnswerRow

case class EstablisherUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(EstablisherNoUTRReasonId(index))
}

object EstablisherUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[EstablisherUTRId] = {

    def establisherName(index: Int) = userAnswers.get(EstablisherNameId(index)).fold(messages("messages__thePerson"))
    (_.fullName)

    def label(index: Int): String = messages("messages__enterUTR", establisherName(index))

    def hiddenLabel(index: Int): String = messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference",
      establisherName(index))

    new CheckYourAnswers[EstablisherUTRId] {
      override def row(id: EstablisherUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[EstablisherUTRId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: EstablisherUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[EstablisherUTRId](label(id.index), hiddenLabel(id.index))().updateRow(id)(changeUrl,
              userAnswers)
        }
      }
    }
  }
}
