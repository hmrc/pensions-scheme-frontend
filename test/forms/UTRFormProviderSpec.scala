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

package forms

import forms.behaviours.UtrBehaviour
import models.ReferenceValue
import play.api.data.Form

class UTRFormProviderSpec extends UtrBehaviour {

  private val requiredKey = "messages__utr__error_required"
  private val invalidKey = "messages__utr__error_invalid"

  private val fieldName: String = "utr"

  "A form with a Utr" should {
    val testForm = new UTRFormProvider().apply()

    behave like formWithUniqueTaxReference[ReferenceValue](
      testForm: Form[ReferenceValue],
      fieldName = fieldName,
      requiredKey: String,
      invalidKey: String
    )

    "remove spaces for valid value with 10 digits and starting with K" in {
      val actual = testForm.bind(Map(fieldName -> "K  123 456 7890 "))
      actual.errors.isEmpty mustBe true
      actual.value mustBe Some(ReferenceValue("K1234567890"))
    }

    "remove spaces for valid value with 10 digits and ending with K" in {
      val actual = testForm.bind(Map(fieldName -> "  123 456 7890 K "))
      actual.errors.isEmpty mustBe true
      actual.value mustBe Some(ReferenceValue("1234567890K"))
    }

    "remove spaces for valid value with 13 digits and starting with K" in {
      val actual = testForm.bind(Map(fieldName -> "K  123 456 7890 1 2 3 "))
      actual.errors.isEmpty mustBe true
      actual.value mustBe Some(ReferenceValue("K1234567890123"))
    }

    "remove spaces for valid value with 13 digits and ending with K" in {
      val actual = testForm.bind(Map(fieldName -> "  123 456 7890 1 2 3 K "))
      actual.errors.isEmpty mustBe true
      actual.value mustBe Some(ReferenceValue("1234567890123K"))
    }

  }
}
