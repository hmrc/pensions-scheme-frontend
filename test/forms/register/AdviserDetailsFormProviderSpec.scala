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

import forms.behaviours.{EmailBehaviours, PhoneNumberBehaviours, StringFieldBehaviours}
import forms.mappings.Constraints
import models.register.AdviserDetails
import org.scalatest.OptionValues
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class AdviserDetailsFormProviderSpec extends  StringFieldBehaviours with Constraints with OptionValues with EmailBehaviours with PhoneNumberBehaviours {


  val form = new AdviserDetailsFormProvider()()
  val nameLength: Int = 107

  ".adviserName" must {
    val fieldName = "adviserName"
    val requiredKey = "messages__adviserDetails__error__name_required"
    val lengthKey = "messages__adviserDetails__error__adviser_name_length"
    val invalidKey = "messages__adviserDetails__error__adviser_name_invalid"
    val maxLength = nameLength

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

  ".emailAddress" must {

    val fieldName = "emailAddress"
    val keyEmailRequired = "messages__error__email"
    val keyEmailLength = "messages__error__email_length"
    val keyEmailInvalid = "messages__error__email_invalid"

    behave like formWithEmailField(
      form,
      fieldName,
      keyEmailRequired,
      keyEmailLength,
      keyEmailInvalid
    )
  }

  ".phoneNumber" must {
    val fieldName = "phoneNumber"
    val keyPhoneNumberRequired = "messages__error__phone"
    val keyPhoneNumberLength = "messages__error__phone_length"
    val keyPhoneNumberInvalid = "messages__error__phone_invalid"

    behave like formWithPhoneNumberField(
      form,
      fieldName,
      keyPhoneNumberRequired,
      keyPhoneNumberLength,
      keyPhoneNumberInvalid
    )
  }

  "form" must {
    val rawData = Map("adviserName" -> "adviser name", "emailAddress" -> "test@test.com", "phoneNumber" -> " 123456789012345678901234 ")
    val expectedData = AdviserDetails("adviser name", "test@test.com", "123456789012345678901234")

    behave like formWithTransform(
      form,
      rawData,
      expectedData
    )
  }
}
