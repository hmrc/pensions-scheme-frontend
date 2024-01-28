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

package forms.register.establishers

import forms.FormSpec

class AddEstablisherFormProviderSpec extends FormSpec {

  val requiredKey = "messages__addEstablisher_error__selection"
  val invalidKey = "error.boolean"

  val formProvider = new AddEstablisherFormProvider()

  "AddEstablisher Form Provider" must {

    "establishers is not empty" when {

      "bind Some(true)" in {
        val form = formProvider(Seq(0)).bind(Map("value" -> "true"))
        form.get mustEqual Some(true)
      }

      "bind Some(false)" in {
        val form = formProvider(Seq(0)).bind(Map("value" -> "false"))
        form.get mustEqual Some(false)
      }

      "fail to bind non-booleans" in {
        val expectedError = error("value", invalidKey)
        checkForError(formProvider(Seq(0)), Map("value" -> "not a boolean"), expectedError)
      }

      "fail to bind a blank value" in {
        val expectedError = error("value", requiredKey)
        checkForError(formProvider(Seq(0)), Map("value" -> ""), expectedError)
      }

      "fail to bind when value is omitted" in {
        val expectedError = error("value", requiredKey)
        checkForError(formProvider(Seq(0)), emptyForm, expectedError)
      }
    }

    "establishers is empty" when {

      "bind Some(true)" in {
        val form = formProvider(Seq.empty).bind(Map("value" -> "true"))
        form.get mustEqual Some(true)
      }

      "bind Some(false)" in {
        val form = formProvider(Seq.empty).bind(Map("value" -> "false"))
        form.get mustEqual Some(false)
      }

      "bind None" in {
        val form = formProvider(Seq.empty).bind(Map.empty[String, String])
        form.get mustNot be(defined)
      }

      "fail to bind non-booleans" in {
        val expectedError = error("value", invalidKey)
        checkForError(formProvider(Seq.empty), Map("value" -> "not a boolean"), expectedError)
      }
    }
  }
}
