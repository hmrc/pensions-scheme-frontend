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

package forms.register

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import org.scalatest.OptionValues
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class PartnershipDetailsFormProviderSpec extends StringFieldBehaviours with Constraints with OptionValues {

  val form = new PartnershipDetailsFormProvider()()

  val partnershipNameLength: Int = 160

  "PartnershipDetailsFormProvider" must {

    val fieldName = "partnershipName"
    val requiredKey = "messages__partnershipDetails__error__required"
    val lengthKey = "messages__partnershipDetails__error_too_long"
    val invalidKey = "messages__partnershipDetails__invalid"
    val maxLength = partnershipNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "{invalid}",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )
  }
}
