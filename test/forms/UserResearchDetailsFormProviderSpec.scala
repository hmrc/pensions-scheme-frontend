/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.behaviours.{EmailBehaviours, StringFieldBehaviours}
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class UserResearchDetailsFormProviderSpec extends StringFieldBehaviours with EmailBehaviours {

  val form = new UserResearchDetailsFormProvider()()

  ".name" must {

    val fieldName = "name"
    val requiredKey = "messages__userResearchDetails__error_name_required"
    val lengthKey = "messages__userResearchDetails__error_name_length"
    val invalidKey = "messages__userResearchDetails__error_name_invalid"
    val maxLength = UserResearchDetailsFormProvider.nameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexUserResearch)
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
      "1234",
      FormError(fieldName, invalidKey, Seq(regexUserResearch))
    )

  }

  ".email" must {

    val fieldName = "email"
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
}
