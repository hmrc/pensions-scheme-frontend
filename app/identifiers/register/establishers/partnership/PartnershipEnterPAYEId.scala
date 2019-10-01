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

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipEnterPAYEId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipEnterPAYEId.toString
}

object PartnershipEnterPAYEId {
  override def toString: String = "partnershipPaye"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnershipEnterPAYEId] = {
    new CheckYourAnswers[PartnershipEnterPAYEId] {

      def getLabel(index: Int, ua: UserAnswers): (String, String) = {
        val partnershipName = ua.get(PartnershipDetailsId(index)).fold(messages("messages__thePartnership"))(_.name)
        (messages("messages__enterPAYE", partnershipName),
          messages("messages__visuallyhidden__dynamic_paye_reference", partnershipName))
      }

      override def row(id: PartnershipEnterPAYEId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        ReferenceValueCYA[PartnershipEnterPAYEId](label, hiddenLabel)().row(id)(changeUrl, ua)
      }

      override def updateRow(id: PartnershipEnterPAYEId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, hiddenLabel) = getLabel(id.index, ua)
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, ua)
          case _ =>
            ReferenceValueCYA[PartnershipEnterPAYEId](label, hiddenLabel)().updateRow(id)(changeUrl, ua)
        }
      }
    }
  }
}






