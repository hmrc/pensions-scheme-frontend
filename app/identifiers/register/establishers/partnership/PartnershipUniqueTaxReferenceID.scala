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
import models.UniqueTaxReference
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class PartnershipUniqueTaxReferenceID(index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipUniqueTaxReferenceID.toString
}

object PartnershipUniqueTaxReferenceID {
  override def toString: String = "partnershipUniqueTaxReference"

  implicit val cya: CheckYourAnswers[PartnershipUniqueTaxReferenceID] = {
    val label = "messages__partnership__checkYourAnswers__utr"
    val utrLabel = "messages__establisher_individual_utr_cya_label"
    val reasonLabel = "messages__partnership__checkYourAnswers__utr_no_reason"
    val changeHasUtr = "messages__visuallyhidden__partnership__utr_yes_no"
    val changeUtr = "messages__visuallyhidden__partnership__utr"
    val changeNoUtr = "messages__visuallyhidden__partnership__utr_no"

    new CheckYourAnswers[PartnershipUniqueTaxReferenceID] {
      override def row(id: PartnershipUniqueTaxReferenceID)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        UniqueTaxReferenceCYA(label, utrLabel, reasonLabel, changeHasUtr, changeUtr, changeNoUtr)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipUniqueTaxReferenceID)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => UniqueTaxReferenceCYA(label, utrLabel, reasonLabel, changeHasUtr, changeUtr, changeNoUtr)().
            row(id)(changeUrl, userAnswers)
          case _ => UniqueTaxReferenceCYA(label, utrLabel, reasonLabel, changeHasUtr, changeUtr, changeNoUtr)().
            updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
