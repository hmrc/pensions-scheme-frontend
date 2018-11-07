/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.register.trustees.TrusteesId
import models.UniqueTaxReference
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}

case class PartnershipUniqueTaxReferenceId(index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipUniqueTaxReferenceId.toString
}

object PartnershipUniqueTaxReferenceId {
  override def toString: String = "partnershipUniqueTaxReference"

  implicit val cya: CheckYourAnswers[PartnershipUniqueTaxReferenceId] =
    UniqueTaxReferenceCYA(
      label = "messages__partnership__checkYourAnswers__utr",
      changeHasUtr = "messages__visuallyhidden__partnership__utr_yes_no",
      changeUtr = "messages__visuallyhidden__partnership__utr",
      changeNoUtr = "messages__visuallyhidden__partnership__utr_no"
    )()
}
