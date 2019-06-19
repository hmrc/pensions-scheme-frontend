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

package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipPayeVariationsId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipPayeVariationsId.toString
}

object PartnershipPayeVariationsId {
  override def toString: String = "partnershipPaye"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnershipPayeVariationsId] = {
    new CheckYourAnswers[PartnershipPayeVariationsId] {

      private val payeLabel = "messages__common__cya__paye"
      private val hiddenLabelPaye = "messages__visuallyhidden__trustee__paye_number"

      override def row(id: PartnershipPayeVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[PartnershipPayeVariationsId](payeLabel, hiddenLabelPaye)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipPayeVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[PartnershipPayeVariationsId](payeLabel, hiddenLabelPaye)().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[PartnershipPayeVariationsId](payeLabel, hiddenLabelPaye)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}






