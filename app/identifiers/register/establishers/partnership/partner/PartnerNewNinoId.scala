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

package identifiers.register.establishers.partnership.partner

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartners, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnerNewNinoId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerNinoId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(PartnerNoNINOReasonId(establisherIndex, partnerIndex))
}

object PartnerNewNinoId {
  override def toString: String = "partnerNino"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerNewNinoId] = {

    new CheckYourAnswersPartners[PartnerNewNinoId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__enterNINO")

      private def hiddenText(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_national_insurance_number")

      override def row(id: PartnerNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[PartnerNewNinoId](label(id.establisherIndex, id.partnerIndex, userAnswers),
          hiddenText(id.establisherIndex, id.partnerIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) =>
            ReferenceValueCYA[PartnerNewNinoId](label(id.establisherIndex, id.partnerIndex, userAnswers),
              hiddenText(id.establisherIndex, id.partnerIndex, userAnswers))()
              .row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[PartnerNewNinoId](label(id.establisherIndex, id.partnerIndex, userAnswers),
              hiddenText(id.establisherIndex, id.partnerIndex, userAnswers))()
              .updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}




