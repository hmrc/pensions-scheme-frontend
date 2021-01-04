/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.mappings.{PayeMapping, RegexBehaviourSpec}
import models.ReferenceValue
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

class PayeBehaviours extends FormSpec with PayeMapping with RegexBehaviourSpec {

  def formWithPayeVariations(
      testForm: Form[ReferenceValue],
      keyPayeRequired: String,
      keyPayeLength: String
  ): Unit = {

    "fail to bind when paye exceeds max length of 16" in {
      val testString = RandomStringUtils.randomAlphabetic(PayeMapping.maxPayeLength + 1)
      val result     = testForm.bind(Map("paye" -> testString))
      result.errors mustBe Seq(FormError("paye", keyPayeLength, Seq(PayeMapping.maxPayeLength)))
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("paye", keyPayeRequired)
      checkForError(testForm, emptyForm, expectedError)
    }

    val valid = Table(
      "data",
      Map("paye" -> " 123/AB56789 "),
      Map("paye" -> " 123AB56789 "),
      Map("paye" -> " 123\\AB56789 ")
    )

    val invalid = Table(
      "data",
      Map("paye" -> "A1_"),
      Map("paye" -> "ABC1234567897"),
      Map("paye" -> "123")
    )

    behave like formWithRegex(testForm, valid, invalid)
  }

}
