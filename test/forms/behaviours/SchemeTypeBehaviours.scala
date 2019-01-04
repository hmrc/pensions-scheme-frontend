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

package forms.behaviours

import forms.FormSpec
import forms.mappings.SchemeTypeMapping
import generators.Generators
import models.register.SchemeType
import org.scalatest.prop.PropertyChecks
import play.api.data.{Form, FormError}

trait SchemeTypeBehaviours extends FormSpec with SchemeTypeMapping with Generators with PropertyChecks {

  private val schemeTypeTable = Table(
    ("type", "value"),
    (SchemeType.SingleTrust, "single"),
    (SchemeType.BodyCorporate, "corp"),
    (SchemeType.GroupLifeDeath, "group"),
    (SchemeType.MasterTrust, "master")
  )

  def formWithSchemeType(testForm: Form[SchemeType],
                         requiredTypeKey: String,
                         invalidTypeKey: String,
                         requiredOtherKey: String,
                         lengthOtherKey: String,
                         invalidOtherKey: String
                        ): Unit = {

    "schemeType" must {
      val maxlength = 160
      val schemeTypeFieldName = "schemeType.type"
      val otherDetailsFieldName = "schemeType.schemeTypeDetails"

      val testForm: Form[SchemeType] = Form(
        "schemeType" -> schemeTypeMapping(requiredTypeKey, invalidTypeKey,
          requiredOtherKey, lengthOtherKey, invalidOtherKey)
      )

      forAll(schemeTypeTable) { (schemeType, schemeValue) =>
        s"bind a valid schemeType $schemeType" in {
          val result = testForm.bind(Map("schemeType.type" -> schemeValue))
          result.get shouldBe schemeType
        }
      }

      "bind a valid schemeType Other" in {
        val result = testForm.bind(Map(schemeTypeFieldName -> "other", otherDetailsFieldName -> "some value"))
        result.get shouldBe SchemeType.Other("some value")
      }

      "not bind an empty Map" in {
        val result = testForm.bind(Map.empty[String, String])
        result.errors should contain(FormError(schemeTypeFieldName, requiredTypeKey))
      }

      "not bind a Map with invalid schemeType" in {
        val result = testForm.bind(Map(schemeTypeFieldName -> "Invalid"))
        result.errors should contain(FormError(schemeTypeFieldName, invalidTypeKey))
      }

      "not bind a Map with type other but no schemeTypeDetails" in {
        val result = testForm.bind(Map(schemeTypeFieldName -> "other"))
        result.errors should contain(FormError(otherDetailsFieldName, requiredOtherKey))
      }

      "not bind a Map with type other and schemeTypeDetails exceeds max length 160" in {
        forAll(stringsLongerThan(maxlength) -> "longString") {
          string =>
            val result = testForm.bind(Map(schemeTypeFieldName -> "other", otherDetailsFieldName -> string))
            result.errors should contain(FormError(otherDetailsFieldName, lengthOtherKey,
              Seq(maxlength)))
        }
      }

      "not bind a Map with type other and invalid schemeTypeDetails" in {
        val result = testForm.bind(Map(schemeTypeFieldName -> "other", otherDetailsFieldName -> "{invalid}"))
        result.errors should contain(FormError(otherDetailsFieldName, invalidOtherKey,
          Seq(regexSafeText)))
      }

      forAll(schemeTypeTable) { (schemeType, schemeValue) =>
        s"unbind a valid schemeType $schemeType" in {
          val result = testForm.fill(schemeType)
          result.apply(schemeTypeFieldName).value.value shouldBe schemeValue
        }
      }

      "unbind a valid schemeType Other" in {
        val result = testForm.fill(SchemeType.Other("some value"))
        result.apply(schemeTypeFieldName).value.value shouldBe "other"
        result.apply(otherDetailsFieldName).value.value shouldBe "some value"
      }
    }
  }
}
