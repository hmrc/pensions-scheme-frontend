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

package forms.behaviours

import forms.FormSpec
import forms.mappings.SchemeTypeMapping
import models.register.SchemeType
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

trait SchemeTypeBehaviours extends FormSpec with SchemeTypeMapping {

  def formWithSchemeType(testForm: Form[SchemeType],
                         requiredTypeKey: String,
                         invalidTypeKey: String,
                         requiredOtherKey: String,
                         invalidOtherKey: String
                        ): Unit = {


    "schemeType" must {
      val validSchemeTypeDetailsLength = 150
      val invalidSchemeTypeDetailsLength = 151

      val testForm: Form[SchemeType] = Form(
        "schemeType" -> schemeTypeMapping("schemeType.error.required", "schemeType.error.invalid",
          "messages__error__scheme_type_information", "messages__error__scheme_type_length")
      )

      "bind a valid schemeType SingleTrust" in {
        val result = testForm.bind(Map("schemeType.type" -> "single"))
        result.get shouldBe SchemeType.SingleTrust
      }

      "bind a valid schemeType GroupLifeDeath" in {
        val result = testForm.bind(Map("schemeType.type" -> "group"))
        result.get shouldBe SchemeType.GroupLifeDeath
      }

      "bind a valid schemeType BodyCorporate" in {
        val result = testForm.bind(Map("schemeType.type" -> "corp"))
        result.get shouldBe SchemeType.BodyCorporate
      }

      "bind a valid schemeType Other" in {
        val result = testForm.bind(Map("schemeType.type" -> "other", "schemeType.schemeTypeDetails" -> "some value"))
        result.get shouldBe SchemeType.Other("some value")
      }

      "not bind an empty Map" in {
        val result = testForm.bind(Map.empty[String, String])
        result.errors should contain(FormError("schemeType.type", "schemeType.error.required"))
      }

      "not bind a Map with invalid schemeType" in {
        val result = testForm.bind(Map("schemeType.type" -> "Invalid"))
        result.errors should contain(FormError("schemeType.type", "schemeType.error.invalid"))
      }

      "not bind a Map with type other but no schemeTypeDetails" in {
        val result = testForm.bind(Map("schemeType.type" -> "other"))
        result.errors should contain(FormError("schemeType.schemeTypeDetails", "messages__error__scheme_type_information"))
      }

      "not bind a Map with type other and schemeTypeDetails exceeds max length 150" in {
        val testString = RandomStringUtils.random(invalidSchemeTypeDetailsLength)
        val result = testForm.bind(Map("schemeType.type" -> "other", "schemeType.schemeTypeDetails" -> testString))
        result.errors should contain(FormError("schemeType.schemeTypeDetails", "messages__error__scheme_type_length",
          Seq(validSchemeTypeDetailsLength)))
      }

      "unbind a valid schemeType SingleTrust" in {
        val result = testForm.fill(SchemeType.SingleTrust)
        result.apply("schemeType.type").value.value shouldBe "single"
      }

      "unbind a valid schemeType GroupLifeDeath" in {
        val result = testForm.fill(SchemeType.GroupLifeDeath)
        result.apply("schemeType.type").value.value shouldBe "group"
      }

      "unbind a valid schemeType BodyCorporate" in {
        val result = testForm.fill(SchemeType.BodyCorporate)
        result.apply("schemeType.type").value.value shouldBe "corp"
      }

      "unbind a valid schemeType Other" in {
        val result = testForm.fill(SchemeType.Other("some value"))
        result.apply("schemeType.type").value.value shouldBe "other"
        result.apply("schemeType.schemeTypeDetails").value.value shouldBe "some value"
      }
    }
  }
}
