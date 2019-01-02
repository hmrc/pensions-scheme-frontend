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

package forms.mappings

import forms.behaviours.UtrBehaviour
import models.UniqueTaxReference
import play.api.data.Form

class UtrMappingSpec extends UtrBehaviour {

  private val requiredKey = "error.required"
  private val requiredUtrKey = "error.utr.required"
  private val requiredReasonKey = "error.reason"
  private val invalidUtrKey = "error.invalid.utr"
  private val maxLengthReasonKey = "error.reason.length"
  private val invalidReasonKey = "error.reason.invalid"


  "UtrMapping" should {
    val testForm: Form[UniqueTaxReference] = Form("uniqueTaxReference" -> uniqueTaxReferenceMapping(
      requiredKey,
      requiredUtrKey,
      requiredReasonKey,
      invalidUtrKey,
      maxLengthReasonKey,
      invalidReasonKey
    ))

    behave like formWithUtr(testForm,
      requiredKey,
      requiredUtrKey,
      requiredReasonKey,
      invalidUtrKey,
      maxLengthReasonKey,
      invalidReasonKey
    )

  }
}
