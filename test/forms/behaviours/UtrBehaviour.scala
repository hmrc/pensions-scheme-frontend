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
import forms.mappings.UtrMapping
import models.UniqueTaxReference
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

trait UtrBehaviour extends FormSpec with UtrMapping {

//  scalastyle:off magic.number

  def formWithUtr(testForm: Form[UniqueTaxReference],
                  requiredKey: String,
                  requiredUtrKey: String,
                  requiredReasonKey: String,
                  invalidUtrKey: String,
                  maxLengthReasonKey: String): Unit = {

    "behave like form with UTR" must {

      "bind a valid uniqueTaxReference with utr when yes is selected" in {
        val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "true", "uniqueTaxReference.utr" -> "1234556676"))
        result.get shouldBe UniqueTaxReference.Yes("1234556676")
      }


      "not bind an empty Map" in {
        val result = testForm.bind(Map.empty[String, String])
        result.errors shouldBe Seq(FormError("uniqueTaxReference.hasUtr", requiredKey))
      }

      Seq("1234", "12345678766655", "sdfghjkloi").foreach { utr =>
        s"not bind an invalid utr $utr" in {
          val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "true", "uniqueTaxReference.utr" -> utr))
          result.errors shouldBe Seq(FormError("uniqueTaxReference.utr", invalidUtrKey, Seq(regexUtr)))
        }
      }

      "not bind a uniqueTaxReference without utr when yes is selected" in {
        val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "true"))
        result.errors shouldBe Seq(FormError("uniqueTaxReference.utr", requiredUtrKey))
      }

      "not bind a uniqueTaxReference without reason when no is selected" in {
        val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "false"))
        result.errors shouldBe Seq(FormError("uniqueTaxReference.reason", requiredReasonKey))
      }

      "not bind a reason greater than 150 characters" in {
        val reason = RandomStringUtils.randomAlphabetic(151)
        val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "false", "uniqueTaxReference.reason" -> reason))
        result.errors shouldBe Seq(FormError("uniqueTaxReference.reason", maxLengthReasonKey, Seq(150)))
      }

      "Successfully unbind 'uniqueTaxReference.hasUtr'" in {
        val result = testForm.fill(UniqueTaxReference.Yes("utr")).data
        result should contain("uniqueTaxReference.hasUtr" -> "true")
        result should contain("uniqueTaxReference.utr" -> "utr")
      }

      "Successfully unbind 'uniqueTaxReference.no'" in {
        val result = testForm.fill(UniqueTaxReference.No("reason")).data
        result should contain("uniqueTaxReference.hasUtr" -> "false")
        result should contain("uniqueTaxReference.reason" -> "reason")
      }
    }
  }
}