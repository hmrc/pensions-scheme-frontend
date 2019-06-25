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
import forms.mappings.UtrMapping
import generators.Generators
import models.UniqueTaxReference
import org.scalatest.prop.PropertyChecks
import play.api.data.{Form, FormError}

trait UtrBehaviour extends FormSpec with UtrMapping with PropertyChecks with Generators {

  //  scalastyle:off magic.number

  def formWithUtr(testForm: Form[UniqueTaxReference],
                  requiredKey: String,
                  requiredUtrKey: String,
                  requiredReasonKey: String,
                  invalidUtrKey: String,
                  maxLengthReasonKey: String,
                  invalidReasonKey: String): Unit = {

    "behave like form with UTR" must {

      val hasUtr = "uniqueTaxReference.hasUtr"
      val utr = "uniqueTaxReference.utr"
      val reason = "uniqueTaxReference.reason"

      Seq("1234556676", " 1234454646 ").foreach {
        utrNo =>
          s"bind a valid uniqueTaxReference with utr $utrNo when yes is selected" in {
            val result = testForm.bind(Map(hasUtr -> "true", utr -> utrNo))
            result.get shouldBe UniqueTaxReference.Yes(utrNo.trim)
          }
      }

      "fail to bind" when {
        "an empty Map" in {
          val result = testForm.bind(Map.empty[String, String])
          result.errors shouldBe Seq(FormError(hasUtr, requiredKey))
        }
        Seq("1234", "12345678766655", "sdfghjkloi").foreach { utrNo =>
          s"utr $utrNo is invalid" in {
            val result = testForm.bind(Map(hasUtr -> "true", utr -> utrNo))
            result.errors shouldBe Seq(FormError(utr, invalidUtrKey, Seq(regexUtr)))
          }
        }
        "yes is selected but no utr is entered" in {
          val result = testForm.bind(Map(hasUtr -> "true"))
          result.errors shouldBe Seq(FormError(utr, requiredUtrKey))
        }
        "no is selected but without reason" in {
          val result = testForm.bind(Map(hasUtr -> "false"))
          result.errors shouldBe Seq(FormError(reason, requiredReasonKey))
        }
        "reason is more than maxlength 160" in {
          val maxlength = 160
          forAll(stringsLongerThan(maxlength) -> "longerString") {
            string =>
              val result = testForm.bind(Map(hasUtr -> "false", reason -> string))
              result.errors shouldBe Seq(FormError(reason, maxLengthReasonKey, Seq(maxlength)))
          }
        }
        "reason is invalid" in {
          val result = testForm.bind(Map(hasUtr -> "false", reason -> "{reason}]"))
          result.errors shouldBe Seq(FormError(reason, invalidReasonKey, Seq(regexSafeText)))
        }
      }

      "Successfully unbind 'uniqueTaxReference.hasUtr'" in {
        val result = testForm.fill(UniqueTaxReference.Yes("utr")).data
        result should contain(hasUtr -> "true")
        result should contain(utr -> "utr")
      }

      "Successfully unbind 'uniqueTaxReference.no'" in {
        val result = testForm.fill(UniqueTaxReference.No("reason")).data
        result should contain(hasUtr -> "false")
        result should contain(reason -> "reason")
      }
    }
  }

  def formWithUtrString(testForm: Form[String],
                  requiredKey: String,
                  maxLengthKey: String,
                  invalidKey: String): Unit = {

    "behave like form with UTR" must {

      val utr = "uniqueTaxReference.utr"

      Seq("1234556676", " 1234454646 ").foreach {
        utrNo =>
          s"bind a valid uniqueTaxReference with utr $utrNo" in {
            val result = testForm.bind(Map(utr -> utrNo))
            result.get shouldBe utrNo.trim
          }
      }

      "fail to bind" when {
        "an empty Map" in {
          val result = testForm.bind(Map.empty[String, String])
          result.errors shouldBe Seq(FormError(utr, requiredKey))
        }

        Seq("1234", "sdfghjkloi").foreach { utrNo =>
          s"utr $utrNo is invalid" in {
            val result = testForm.bind(Map(utr -> utrNo))
            result.errors shouldBe Seq(FormError(utr, invalidKey, Seq(regexUtr)))
          }
        }

        Seq("12345678766655", "adfghsdfghjkloi").foreach { utrNo =>
          s"utr $utrNo exceeds max length allowed" in {
            val maxLength = 10
            val result = testForm.bind(Map(utr -> utrNo))
            result.errors shouldBe Seq(FormError(utr, maxLengthKey, Seq(maxLength)))
          }
        }

      }

      "Successfully unbind 'utr'" in {
        val result = testForm.fill("utr").data
        result should contain(utr -> "utr")
      }

    }
  }
}