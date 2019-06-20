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

package forms

import forms.behaviours.UtrBehaviour
import play.api.data.Form

class UTRFormProviderSpec extends UtrBehaviour {

  private val requiredKey = "messages__utr__error_required"
  private val maxLengthKey = "messages__utr__error_maxLength"
  private val invalidKey = "messages__utr__error_invalid"

  "A form with a Vat" should {
    val testForm = new UTRFormProvider().apply()

    behave like formWithUtrString(testForm: Form[String],
      requiredKey: String,
      maxLengthKey: String,
      invalidKey: String
    )

  }
}
