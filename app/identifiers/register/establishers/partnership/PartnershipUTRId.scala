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

package identifiers.register.establishers.partnership

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(PartnershipNoUTRReasonId(this.index))
}

object PartnershipUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[PartnershipUTRId] = {

    def getLabel(index: Int, ua: UserAnswers): (String, String) = {
      val partnershipName = ua.get(PartnershipDetailsId(index)).fold(messages("messages__thePartnership"))(_.name)
      (messages("messages__dynamic_whatIsUTR", partnershipName),
        messages("messages__visuallyhidden__dynamic_utr", partnershipName))
    }

    new CheckYourAnswers[PartnershipUTRId] {
      override def row(id: PartnershipUTRId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        ReferenceValueCYA[PartnershipUTRId](label, hiddenLabel)().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipUTRId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, ua)
          case _ =>
            ReferenceValueCYA[PartnershipUTRId](label, hiddenLabel)().updateRow(id)(changeUrl, ua)
        }
      }
    }
  }
}











