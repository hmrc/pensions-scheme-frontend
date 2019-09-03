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

package identifiers.register.trustees.company

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.UniqueTaxReference
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class CompanyUniqueTaxReferenceId(index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = TrusteesId(index).path \ CompanyUniqueTaxReferenceId.toString
}

object CompanyUniqueTaxReferenceId {
  override def toString: String = "companyUniqueTaxReference"

  implicit val cya: CheckYourAnswers[CompanyUniqueTaxReferenceId] = {
    val label = "messages__checkYourAnswers__trustees__company__utr"
    val utrLabel = "messages__cya__utr"
    val reasonLabel = "messages__checkYourAnswers__trustees__company__utr_no_reason"
    val changeHasUtr = "messages__visuallyhidden__trustee__utr_yes_no"
    val changeUtr = "messages__visuallyhidden__trustee__utr"
    val changeNoUtr = "messages__visuallyhidden__trustee__utr_no"

    new CheckYourAnswers[CompanyUniqueTaxReferenceId] {
      override def row(id: CompanyUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        UniqueTaxReferenceCYA(label, utrLabel, reasonLabel, changeHasUtr, changeUtr, changeNoUtr)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: CompanyUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => UniqueTaxReferenceCYA(label, utrLabel, reasonLabel, changeHasUtr, changeUtr, changeNoUtr)().row(id)(changeUrl, userAnswers)
          case _ => UniqueTaxReferenceCYA(label, utrLabel, reasonLabel, changeHasUtr, changeUtr, changeNoUtr)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
