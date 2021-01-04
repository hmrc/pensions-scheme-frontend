/*
 * Copyright 2021 HM Revenue & Customs
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

package identifiers.register.establishers.partnership.partner

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.ReferenceValue
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartners, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class PartnerEnterUTRId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath =
    EstablishersId(establisherIndex)
      .path \ "partner" \ partnerIndex \ PartnerEnterUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(PartnerNoUTRReasonId(establisherIndex, partnerIndex))
}

object PartnerEnterUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnerEnterUTRId] = {

    new CheckYourAnswersPartners[PartnerEnterUTRId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):Message =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__enterUTR")

      private def hiddenLabel(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):Message =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_unique_taxpayer_reference")

      override def row(id: PartnerEnterUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA(label(id.establisherIndex, id.partnerIndex, userAnswers),
          hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)


      override def updateRow(id: PartnerEnterUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => ReferenceValueCYA(label(id.establisherIndex, id.partnerIndex, userAnswers),
            hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers))()
            .updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}







