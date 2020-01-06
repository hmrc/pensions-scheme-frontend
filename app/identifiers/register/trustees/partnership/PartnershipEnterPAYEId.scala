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

package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipEnterPAYEId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipEnterPAYEId.toString
}

object PartnershipEnterPAYEId {
  override def toString: String = "partnershipPaye"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnershipEnterPAYEId] = {
    new CheckYourAnswers[PartnershipEnterPAYEId] {

      def trusteeName(index: Int) = userAnswers.get(PartnershipDetailsId(index)).fold(messages("messages__theTrustee"))(_.name)
      def label(index: Int) = messages("messages__enterPAYE", trusteeName(index))
      def hiddenLabel(index: Int) = messages("messages__visuallyhidden__dynamic_paye_reference", trusteeName(index))

      override def row(id: PartnershipEnterPAYEId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[PartnershipEnterPAYEId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipEnterPAYEId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[PartnershipEnterPAYEId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[PartnershipEnterPAYEId](label(id.index), hiddenLabel(id.index))().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}






