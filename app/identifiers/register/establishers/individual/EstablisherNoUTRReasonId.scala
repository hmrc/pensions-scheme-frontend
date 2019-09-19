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
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class EstablisherNoUTRReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherNoUTRReasonId.toString
}

object EstablisherNoUTRReasonId {
  override def toString: String = "noUtrReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[EstablisherNoUTRReasonId] = {

    def establisherName(index: Int) = userAnswers.get(EstablisherNameId(index)).fold(messages("messages__theEstablisher"))(_.fullName)
    def label(index: Int) = Some(messages("messages__noGenericUtr__heading", establisherName(index)))
    def hiddenLabel(index: Int) = Some(messages("messages__visuallyhidden__dynamic_noUtrReason", establisherName(index)))

    new CheckYourAnswers[EstablisherNoUTRReasonId] {
      override def row(id: EstablisherNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)


      override def updateRow(id: EstablisherNoUTRReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
