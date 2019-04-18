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
import models.UniqueTaxReference
import play.api.libs.json.{JsPath, Reads}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class PartnerUniqueTaxReferenceId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerUniqueTaxReferenceId.toString
}

object PartnerUniqueTaxReferenceId {
  override def toString: String = "partnerUniqueTaxReference"

  implicit def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]](implicit rds: Reads[UniqueTaxReference]):
  CheckYourAnswers[PartnerUniqueTaxReferenceId] = {
    val inLabel = "messages__partner_utr_question_cya_label"
    val inChangeHasUtr = "messages__visuallyhidden__partner__utr_yes_no"
    val inChangeUtr = "messages__visuallyhidden__partner__utr"
    val inChangeNoUtr = "messages__visuallyhidden__partner__utr_no"

    new CheckYourAnswers[PartnerUniqueTaxReferenceId] {

      override def row(id: PartnerUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        UniqueTaxReferenceCYA[PartnerUniqueTaxReferenceId](label = inLabel, changeHasUtr = inChangeHasUtr,
          changeUtr = inChangeUtr, changeNoUtr = inChangeNoUtr)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => UniqueTaxReferenceCYA[PartnerUniqueTaxReferenceId](label = inLabel, changeHasUtr = inChangeHasUtr,
            changeUtr = inChangeUtr, changeNoUtr = inChangeNoUtr)().row(id)(changeUrl, userAnswers)
          case _ => UniqueTaxReferenceCYA[PartnerUniqueTaxReferenceId](label = inLabel, changeHasUtr = inChangeHasUtr,
            changeUtr = inChangeUtr, changeNoUtr = inChangeNoUtr)().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
