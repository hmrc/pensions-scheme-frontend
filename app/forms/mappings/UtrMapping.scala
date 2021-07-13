/*
 * Copyright 2021 HM Revenue & Customs
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

trait UtrMapping extends Mappings with Transforms {

  private val utrMaxLength = 10 to 14

  def utrMapping(requiredKey: String = "messages__utr__error_required",
                 maxLengthKey: String = "messages__utr__error_maxLength",
                 invalidKey: String = "messages__utr__error_invalid"
                ): Mapping[String] = text(requiredKey)
    .transform(strip, noTransform)
    .verifying(firstError(maxMinLength(utrMaxLength, maxLengthKey),
      regexp(regexUtr, invalidKey)))
}
