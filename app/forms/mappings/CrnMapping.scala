/*
 * Copyright 2024 HM Revenue & Customs
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

trait CrnMapping extends Mappings with Transforms {

  def crnMapping(crnLengthKey: String = "messages__error__no_crn_length",
                 requiredCRNKey: String = "messages__error__company_number",
                 invalidCRNKey: String = "messages__error__crn_invalid"):
  Mapping[String] = text(requiredCRNKey)
    .transform(noSpaceWithUpperCaseTransform, noTransform)
    .verifying(validCrn(invalidCRNKey))
}
