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
import forms.mappings.{CrnMapping, Transforms}
import generators.Generators
import models.ReferenceValue
import org.scalatest.prop.PropertyChecks
import play.api.data.{Form, FormError}

trait CrnBehaviour extends FormSpec with CrnMapping with PropertyChecks with Generators with Transforms {

  def formWithCrnVariations(testForm: Form[ReferenceValue],
                            crnLengthKey: String,
                            requiredCRNKey: String,
                            invaliddCRNKey: String
                           ): Unit = {

    "behave like a form with a companyRegistrationNumber Mapping in variations" must {

      Seq("1234567", " 1234 567 ").foreach {
        crnNo =>
          s"bind successfully when CRN $crnNo is valid" in {
            val result = testForm.bind(Map("companyRegistrationNumber" -> crnNo))
            result.errors.size mustBe 0
            result.get mustBe ReferenceValue(noSpaceWithUpperCaseTransform(crnNo))
          }
      }

      "fail to bind when value is not entered" in {
        val expectedError = error("companyRegistrationNumber", requiredCRNKey)
        checkForError(testForm, emptyForm, expectedError)
      }

      Seq("1234567891", "123.456").foreach { crn =>
        s"fail to bind when companyRegistrationNumber $crn is longer than expected" in {
          val result = testForm.bind(Map("companyRegistrationNumber" -> crn))
          result.errors mustBe Seq(FormError("companyRegistrationNumber", invaliddCRNKey))
        }
      }

    }
  }
}
