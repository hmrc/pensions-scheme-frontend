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

package forms.register

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class BenefitsInsurerFormProviderSpec extends StringFieldBehaviours with Constraints {
  val form = new BenefitsInsurerFormProvider()()

  ".companyName" must {
    val validMaxLength = 160

    val fieldName = "companyName"
    val lengthKey = "messages__error__company_name_length"
    val requiredErrorKey = "messages__error__company_name"
    val invalidKey = "messages__error__company_name_invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredErrorKey)
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
      "[[company Name]]",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )
  }

  ".policyNumber" must {
    val validMaxLength = 55
    val fieldName = "policyNumber"
    val requiredKey = "messages__error__benefits_insurance__policy"
    val lengthKey = "messages__error__benefits_insurance__policy_length"
    val invalidKey = "messages__error__benefits_insurance__policy_invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText)
    )

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
      "[[policy number]]",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )
  }
}
