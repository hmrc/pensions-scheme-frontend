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

package forms.behaviours

import forms.FormSpec
import play.api.data.{Form, FormError}

trait CheckboxBehaviour extends FormSpec {

  def formWithCheckbox(form: Form[_], fieldName: String, trueValue: String, acceptTrueOnly: Boolean, invalidKey: String): Unit = {

    "behave like a form with a checkbox" must {
      "transform a valid value to true" in {
        val result = form.bind(Map(fieldName -> trueValue))
        result.errors.size mustBe 0
        result.get mustBe true
      }

      if (acceptTrueOnly) {
        "reject a blank value" in {
          val result = form.bind(Map(fieldName -> ""))
          result.errors mustBe List(FormError(fieldName, invalidKey))
        }

        "reject a missing value" in {
          val result = form.bind(Map.empty[String, String])
          result.errors mustBe List(FormError(fieldName, invalidKey))
        }
      }
      else {
        "transform a blank value to false" in {
          val result = form.bind(Map(fieldName -> ""))
          result.errors.size mustBe 0
          result.get mustBe false
        }

        "transform missing value to false" in {
          val result = form.bind(Map.empty[String, String])
          result.errors.size mustBe 0
          result.get mustBe false
        }
      }

      "reject an invalid value" in {
        val result = form.bind(Map(fieldName -> s"$trueValue-invalid"))
        result.errors mustBe List(FormError(fieldName, invalidKey))
      }
    }

  }

}
