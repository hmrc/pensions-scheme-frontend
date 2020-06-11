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

package identifiers.register.establishers.company

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class CompanyNoCRNReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ CompanyNoCRNReasonId.toString
}

object CompanyNoCRNReasonId {
  override def toString: String = "noCrnReason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   countryOptions: CountryOptions): CheckYourAnswers[CompanyNoCRNReasonId] = {

    new CheckYourAnswersCompany[CompanyNoCRNReasonId] {

      private def label(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__whyNoCRN")

      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_noCrnReason")

      override def row(id: CompanyNoCRNReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(id.index, userAnswers)),
          Some(hiddenLabel(id.index, userAnswers)))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyNoCRNReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
