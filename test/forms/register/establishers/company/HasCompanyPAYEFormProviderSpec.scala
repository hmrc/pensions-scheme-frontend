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

package forms.register.establishers.company

import forms.FormSpec

class HasCompanyPAYEFormProviderSpec extends FormSpec {
  val requiredKey = "messages__companyPayeRef__error__required"
  val invalidKey = "error.boolean"
  val validData: Map[String, String] = Map(
    "value" -> "true"
  )

  val formProvider = new HasCompanyPAYEFormProvider()

  "HasCompanyPAYEFormProvider form" must {

    "bind true" in {
      val form = formProvider().bind(Map("hasPaye" -> "true"))
      form.get shouldBe true
    }

    "bind false" in {
      val form = formProvider().bind(Map("hasPaye" -> "false"))
      form.get shouldBe false
    }

    "fail to bind non-booleans" in {
      val expectedError = error("hasPaye", invalidKey)
      checkForError(formProvider(), Map("hasPaye" -> "not a boolean"), expectedError)
    }

    "fail to bind a blank value" in {
      val expectedError = error("hasPaye", requiredKey)
      checkForError(formProvider(), Map("hasPaye" -> ""), expectedError)
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("hasPaye", requiredKey)
      checkForError(formProvider(), emptyForm, expectedError)
    }
  }
}
