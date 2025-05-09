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

package forms.behaviours

import forms.mappings.{Constraints, EmailMapping, RegexBehaviourSpec}
import play.api.data.{Form, FormError}


trait EmailBehaviours extends StringFieldBehaviours with Constraints with EmailMapping with RegexBehaviourSpec{

  def formWithEmailField(
                          form: Form[?],
                          fieldName: String,
                          keyEmailRequired: String,
                          keyEmailLength: String,
                          keyEmailInvalid: String
                        ): Unit = {

    "behave like a form with an email field" must {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        "ab@test.com"
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = EmailMapping.maxEmailLength,
        lengthError = FormError(fieldName, keyEmailLength, Seq(EmailMapping.maxEmailLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyEmailRequired)
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "ab@*.com",
        FormError(fieldName, keyEmailInvalid, Seq(regexEmailRestrictive))
      )

    }
  }

  def formWithEmailFieldForAllErrors(
                          form: Form[?],
                          fieldName: String,
                          keyEmailRequired: String,
                          keyEmailLength: String,
                          keyEmailInvalid: String
                        ): Unit = {

    "behave like a form with an email field" must {

      formWithEmailField(
        form,
        fieldName: String,
        keyEmailRequired: String,
        keyEmailLength: String,
        keyEmailInvalid: String
      )

      behave like emailWithCorrectFormat(
        form,
        fieldName,
        keyEmailInvalid
      )

      behave like formWithRegex(
        form,
        Table(
          "valid",
          Map("email" -> "test@test.com"),
          Map("email" -> "test@t-g.com"),
          Map("email" -> "\"\"@test.com")
        ),
        Table(
          "invalid",
          Map("email" -> "test@sdff"),
          Map("email" -> "test@-.com")
        )
      )
    }
  }

  private def emailWithCorrectFormat(
                                      form: Form[?],
                                      fieldName: String,
                                      keyEmailInvalid: String
                                    ): Unit = {

    def result(value: String) = form.bind(Map(fieldName -> value)).apply(fieldName)

    "not bind when @ sign not included" in {
      result("test.com").errors mustEqual Seq(FormError(fieldName, keyEmailInvalid))
    }

    "not bind when starts with @ sign" in {
      result("@test.com").errors mustEqual Seq(FormError(fieldName, keyEmailInvalid))
    }

    "not bind when nothing is between @ sign and dot(.)" in {
      result("test@.com").errors mustEqual Seq(FormError(fieldName, keyEmailInvalid))
    }

    "not bind when ends with a dot(.)" in {
      result("test@com.").errors mustEqual Seq(FormError(fieldName, keyEmailInvalid))
    }
  }

}
