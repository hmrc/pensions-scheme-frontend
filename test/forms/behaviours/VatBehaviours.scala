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
import forms.mappings.{Constraints, VatMapping}
import generators.Generators
import models.{ReferenceValue, Vat}
import org.scalatest.prop.PropertyChecks
import play.api.data.{Form, FormError}

trait VatBehaviours extends FormSpec with Generators with PropertyChecks with Constraints with VatMapping {

  val maxVatLength = 9

  //scalastyle:off method.length
  def formWithVat(testForm: Form[Vat],
                  requiredKey: String,
                  vatLengthKey: String,
                  requiredVatKey: String,
                  invalidVatKey: String
                 ): Unit = {

    "behave like a form with a VAT Mapping" must {

      "fail to bind when yes is selected but VAT is not provided" in {
        val result = testForm.bind(Map("vat.hasVat" -> "true"))
        result.errors mustBe Seq(FormError("vat.vat", requiredVatKey))
      }

      Seq("AB123490", "AO111111B", "ORA12345C", "AB0202020", "AB040404E").foreach { vat =>
        s"fail to bind when VAT $vat is invalid" in {
          val result = testForm.bind(Map("vat.hasVat" -> "true", "vat.vat" -> vat))
          result.errors mustBe Seq(FormError("vat.vat", invalidVatKey, Seq(regexVat)))
        }
      }

      Seq("AB1234567890", "987654328765", "CDCDCDOPOPOP", "AB03047853030D").foreach { vat =>
        s"fail to bind when VAT $vat is longer than expected" in {
          val result = testForm.bind(Map("vat.hasVat" -> "true", "vat.vat" -> vat))
          result.errors mustBe Seq(FormError("vat.vat", vatLengthKey, Seq(VatMapping.maxVatLength)))
        }
      }

      Seq("9 9 9 9 9 9 9 9 9 ", "999999999").foreach {
        validVat =>
          s"successfully bind when yes is selected and valid VAT $validVat is provided" in {
            val form = testForm.bind(Map("vat.hasVat" -> "true", "vat.vat" -> validVat))
            form.get mustEqual Vat.Yes("999999999")
          }
      }

      "successfully bind when no is selected" in {
        val form = testForm.bind(Map("vat.hasVat" -> "false"))
        form.get mustBe Vat.No
      }

      "fail to bind when value is omitted" in {
        val expectedError = error("vat.hasVat", requiredKey)
        checkForError(testForm, emptyForm, expectedError)
      }

      "successfully unbind `Vat.Yes`" in {
        val result = testForm.fill(Vat.Yes("vat")).data
        result must contain("vat.hasVat" -> "true")
        result must contain("vat.vat" -> "vat")
      }

      "successfully unbind `Vat.No`" in {
        val result = testForm.fill(Vat.No).data
        result must contain("vat.hasVat" -> "false")
      }
    }

    "vatRegistrationNumberTransform" must {
      "strip leading, trailing ,and internal spaces" in {
        val actual = vatRegistrationNumberTransform("  123 456 789  ")
        actual mustBe "123456789"
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

  def formWithVatVariations(testForm: Form[ReferenceValue],
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
