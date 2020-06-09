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

package identifiers.register.establishers.partnership.partner

import identifiers._
import identifiers.register.establishers.EstablishersId
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartners}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class PartnerNoNINOReasonId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \
    PartnerNoNINOReasonId.toString

}

object PartnerNoNINOReasonId {
  override def toString: String = "noNinoReason"

  implicit def cya(implicit messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[PartnerNoNINOReasonId] = {

    new CheckYourAnswersPartners[PartnerNoNINOReasonId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua: UserAnswers): String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__whyNoNINO")

      private def hiddenLabel(establisherIndex: Int, partnerIndex: Int, ua: UserAnswers): String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_noNinoReason")

      override def row(id: PartnerNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(id.establisherIndex, id.partnerIndex, userAnswers)),
          Some(hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers)))()
          .row(id)(changeUrl, userAnswers)


      override def updateRow(id: PartnerNoNINOReasonId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}






