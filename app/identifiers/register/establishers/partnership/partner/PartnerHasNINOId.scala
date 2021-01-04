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

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartners}
import viewmodels.{AnswerRow, Message}

case class PartnerHasNINOId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath =
    EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerHasNINOId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) => userAnswers.remove(PartnerNoNINOReasonId(establisherIndex, partnerIndex))
      case Some(false) => userAnswers.remove(PartnerEnterNINOId(establisherIndex, partnerIndex))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnerHasNINOId {
  override def toString: String = "hasNino"

  implicit def cya: CheckYourAnswers[PartnerHasNINOId] = {

    new CheckYourAnswersPartners[PartnerHasNINOId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):Message =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__hasNINO")

      private def hiddenText(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):Message =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_hasNino")

      override def row(id: PartnerHasNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(Some(label(id.establisherIndex, id.partnerIndex, userAnswers)),
          Some(hiddenText(id.establisherIndex, id.partnerIndex, userAnswers)))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerHasNINOId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => BooleanCYA(Some(label(id.establisherIndex, id.partnerIndex, userAnswers)),
            Some(hiddenText(id.establisherIndex, id.partnerIndex, userAnswers)))()
            .row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}



