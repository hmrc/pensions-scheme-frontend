/*
 * Copyright 2023 HM Revenue & Customs
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

class SchemeNameFormProviderSpec extends StringFieldBehaviours with Constraints {
  val validData: Map[String, String] = Map(
    "schemeName" -> "scheme Name 1")
  val validMaxLength = 160
  val form = new SchemeNameFormProvider()()

  ".schemeName" must {
    val fieldName = "schemeName"
    val lengthKey = "messages__error__scheme_name_length"
    val requiredKey = "messages__error__scheme_name"
    val invalidKey = "messages__error__scheme_name_invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = validMaxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(validMaxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "{name}",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )
  }
}
