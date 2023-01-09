/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ReferenceValue
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany, ReferenceValueCYA}
import viewmodels.{AnswerRow, Message}

case class CompanyEnterUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ CompanyEnterUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(CompanyNoUTRReasonId(this.index))
}

object CompanyEnterUTRId {
  override def toString: String = "utr"

  implicit def cya: CheckYourAnswers[CompanyEnterUTRId] = {

    val label: Message = Message("messages__utr__checkyouranswerslabel")

    new CheckYourAnswersCompany[CompanyEnterUTRId] {

      private def hiddenLabel(index: Int, ua: UserAnswers): Message = {
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_unique_taxpayer_reference")
      }

      override def row(id: CompanyEnterUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA(label, hiddenLabel(id.index, userAnswers))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyEnterUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA(label, hiddenLabel(id.index, userAnswers))()
              .updateRow(id)(changeUrl, userAnswers)
        }
    }
  }

}


