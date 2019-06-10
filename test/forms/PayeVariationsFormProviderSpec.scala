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

import forms.behaviours.PayeBehaviours
import models.Paye
import play.api.data.Form

class PayeVariationsFormProviderSpec extends PayeBehaviours {

  private val requiredPayeKey = "messages__error__paye_required"
  private val payeLengthKey = "messages__error__paye_length"
  private val invalidPayeKey = "messages__error__paye_invalid"

  "A form with Paye" should {
    val mapping = payeStringMapping(
      requiredPayeKey,
      invalidPayeKey,
      payeLengthKey
    )

    val testForm = new PayeVariationsFormProvider().apply()

    behave like formWithPayeVariations(
      testForm: Form[String],
      requiredPayeKey: String,
      payeLengthKey: String
    )

  }
}
