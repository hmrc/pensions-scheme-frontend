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

package forms.mappings

import play.api.data.Mapping

trait VatMapping extends Mappings with Transforms {

  def vatMapping(vatLengthKey: String = "messages__enterVAT__company_invalid",
                 requiredVatKey: String = "messages__error__vat_required",
                 invalidVatKey: String = "messages__enterVAT__company_invalid"):
  Mapping[String] = text(requiredVatKey)
    .transform(vatRegistrationNumberTransform, noTransform)
    .verifying(
      firstError(
        maxLength(VatMapping.maxVatLength, vatLengthKey),
        vatRegistrationNumber(invalidVatKey))
    )
}

object VatMapping {
  val maxVatLength = 9
}
