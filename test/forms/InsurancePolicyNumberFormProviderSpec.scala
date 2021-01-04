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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class InsurancePolicyNumberFormProviderSpec extends StringFieldBehaviours with Constraints {
  val validData: Map[String, String] = Map("policyNumber" -> "test policy number")
  val form                           = new InsurancePolicyNumberFormProvider()()

  "policyNumber" must {
    val validMaxLength = 55
    val fieldName      = "policyNumber"
    val requiredKey    = "messages__error__insurance_policy_number"
    val lengthKey      = "messages__error__insurance_policy_number_length"
    val invalidKey     = "messages__error__insurance_policy_number_invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexPolicyNumber)
    )

    "remove spaces and convert to upper case for valid value" in {
      val result = form.bind(Map(fieldName -> "  a b c ÿ d e f   "))
      result.errors mustBe empty
      result.value mustBe Some("ABCÿDEF")
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = validMaxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(validMaxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "{policy number}",
      error = FormError(fieldName, invalidKey, Seq(regexPolicyNumber))
    )
  }
}
