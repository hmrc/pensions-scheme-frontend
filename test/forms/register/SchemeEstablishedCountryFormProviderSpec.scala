/*
 * Copyright 2017 HM Revenue & Customs
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

import forms.FormSpec

class SchemeEstablishedCountryFormProviderSpec extends FormSpec {

  val requiredKey = "messages__error__scheme_country"

  "SchemeEstablishedCountry Form" must {

    val formProvider = new SchemeEstablishedCountryFormProvider()

    "bind a string" in {
      val form = formProvider().bind(Map("value" -> "answer"))
      form.get shouldBe "answer"
    }

    "fail to bind a blank value" in {
      val expectedError = error("value", requiredKey)
      checkForError(formProvider(), Map("value" -> ""), expectedError)
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("value", requiredKey)
      checkForError(formProvider(), emptyForm, expectedError)
    }
  }
}
