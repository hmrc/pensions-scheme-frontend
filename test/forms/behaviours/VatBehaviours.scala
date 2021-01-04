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
import forms.mappings.{Constraints, VatMapping}
import generators.Generators
import models.ReferenceValue
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

trait VatBehaviours extends FormSpec with Generators with ScalaCheckPropertyChecks with Constraints with VatMapping {

  val maxVatLength = 9

  //scalastyle:off method.length
  def formWithEnterVAT(testForm: Form[ReferenceValue],
                            vatLengthKey: String,
                            requiredVatKey: String,
                            invalidVatKey: String
                 ): Unit = {

    "behave like a form with a VAT Mapping in variations" must {

      "fail to bind when value is not entered" in {
        val expectedError = error("vat", requiredVatKey)
        checkForError(testForm, emptyForm, expectedError)
      }

      Seq("AB123490", "AO111111B", "ORA12345C", "AB0202020", "AB040404E").foreach { vat =>
        s"fail to bind when VAT $vat is invalid" in {
          val result = testForm.bind(Map("vat" -> vat))
          result.errors mustBe Seq(FormError("vat", invalidVatKey, Seq(regexVat)))
        }
      }

      Seq("AB1234567890", "987654328765", "CDCDCDOPOPOP", "AB03047853030D").foreach { vat =>
        s"fail to bind when VAT $vat is longer than expected" in {
          val result = testForm.bind(Map("vat" -> vat))
          result.errors mustBe Seq(FormError("vat", vatLengthKey, Seq(VatMapping.maxVatLength)))
        }
      }

      "vatRegistrationNumberTransform" must {
        "strip leading, trailing ,and internal spaces" in {
          val actual = vatRegistrationNumberTransform("  123 456 789  ")
          actual mustBe "123456789"
        }

        "convert to uppercase" in {
          val actual = vatRegistrationNumberTransform("ab12345678")
          actual mustBe "AB12345678"
        }

        "remove leading GB" in {
          val gb = Table(
            "vat",
            "GB123456789",
            "Gb123456789",
            "gB123456789",
            "gb123456789"
          )

          forAll(gb) { vat =>
            vatRegistrationNumberTransform(vat) mustBe "123456789"
          }
        }
      }
    }
  }

}
