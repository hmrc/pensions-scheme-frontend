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

package forms.dataPrefill

import base.SpecBase
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class DataPrefillCheckboxFormProviderSpec extends BooleanFieldBehaviours with SpecBase {

  private val requiredKey = "required"
  private val noneSelectedWithValueErrorKey = "nonselected"
  private val moreThanTenErrorKey = "moreten"

  private val form = new DataPrefillCheckboxFormProvider().apply(
    entityCount = 2,
    requiredError = requiredKey,
    noneSelectedWithValueError = noneSelectedWithValueErrorKey,
    moreThanTenError = moreThanTenErrorKey
  )

  private val fieldName = "value"

  "Check box form provider" must {
    "bind valid data" in {
      val result = form.bind(Map(
        "value[0]" -> "2",
        "value[1]" -> "4"
      ))
      result.value mustBe Some(List(2,4))
    }

    "fail to bind when empty" in {
      val result = form.bind(Map[String, String]())
      result.errors mustEqual Seq(FormError(fieldName, requiredKey))
    }

    "fail to bind when None selected with value" in {
      val form = new DataPrefillCheckboxFormProvider().apply(
        entityCount = 2,
        requiredError = requiredKey,
        noneSelectedWithValueError = noneSelectedWithValueErrorKey,
        moreThanTenError = moreThanTenErrorKey
      )
      val result = form.bind(Map(
        "value[0]" -> "-1",
        "value[1]" -> "4",
        "value[2]" -> "5"
      ))
      result.errors mustEqual Seq(FormError(fieldName, noneSelectedWithValueErrorKey))
    }

    "bind when trying to add results in exactly ten directors" in {
      val form = new DataPrefillCheckboxFormProvider().apply(
        entityCount = 8,
        requiredError = requiredKey,
        noneSelectedWithValueError = noneSelectedWithValueErrorKey,
        moreThanTenError = moreThanTenErrorKey
      )
      val result = form.bind(Map(
        "value[0]" -> "2",
        "value[1]" -> "4"
      ))
      result.value mustBe Some(List(2,4))
    }

    "fail to bind when trying to add results in more than ten directors" in {
      val form = new DataPrefillCheckboxFormProvider().apply(
        entityCount = 8,
        requiredError = requiredKey,
        noneSelectedWithValueError = noneSelectedWithValueErrorKey,
        moreThanTenError = moreThanTenErrorKey
      )
      val result = form.bind(Map(
        "value[0]" -> "2",
        "value[1]" -> "4",
        "value[2]" -> "5"
      ))
      result.errors mustEqual Seq(FormError(fieldName, moreThanTenErrorKey))
    }
  }
}
