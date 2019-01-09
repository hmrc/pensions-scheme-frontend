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
import forms.mappings.BankDetailsMapping
import models.register.SortCode
import play.api.data.{Form, FormError}

trait BankDetailsBehaviour extends FormSpec with BankDetailsMapping {

  def formWithSortCode[T](testForm: Form[T],
                          keyRequired: String,
                          keyInvalid: String,
                          keyMaxError: String,
                          validOtherData: Map[String, String],
                          getSortCode: (T) => SortCode
                         ): Unit = {


    "behave like form with SortCode" should {

      Seq("12 34 56", "12-34-56", " 123456").foreach { sortCode =>
        s"bind a valid sort code $sortCode" in {
          val result = testForm.bind(validOtherData ++ Map("sortCode" -> sortCode))
          getSortCode(result.get) shouldBe SortCode("12", "34", "56")
        }
      }

      "not bind when sort code is not supplied" in {
        val result = testForm.bind(validOtherData)
        result.errors shouldBe Seq(FormError("sortCode", keyRequired))
      }

      Seq("12%34&56", "abdgfg").foreach { sortCode =>
        s"not bind an invalid sort code $sortCode" in {
          val result = testForm.bind(validOtherData ++ Map("sortCode" -> sortCode))
          result.errors shouldBe Seq(FormError("sortCode", keyInvalid))
        }
      }

      Seq("12 34 56 56", "12345678").foreach { sortCode =>
        s"not bind a sort code $sortCode which exceeds max length" in {
          val result = testForm.bind(validOtherData ++ Map("sortCode" -> sortCode))
          result.errors shouldBe Seq(FormError("sortCode", keyMaxError))
        }
      }

      "not bind a sort code which is less than the expected length" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode" -> "12345"))
        result.errors shouldBe Seq(FormError("sortCode", keyInvalid))
      }
    }
  }

  def formWithSortCodeHS[T](testForm: Form[T],
                          keyRequired: String,
                          keyLengthError: String,
                          validOtherData: Map[String, String],
                          getSortCode: T => SortCode
                         ): Unit = {


    "behave like form with SortCode" should {

      Seq("12 34 56", "12-34-56", " 123456").foreach { sortCode =>
        s"bind a valid sort code $sortCode" in {
          val result = testForm.bind(validOtherData ++ Map("sortCode" -> sortCode))
          getSortCode(result.get) shouldBe SortCode("12", "34", "56")
        }
      }

      "not bind when sort code is not supplied" in {
        val result = testForm.bind(validOtherData)
        result.errors shouldBe Seq(FormError("sortCode", keyRequired))
      }

      Seq("$9J223XXX", "abdgf").foreach { sortCode =>
        s"not bind an invalid sort code $sortCode" in {
          val result = testForm.bind(validOtherData ++ Map("sortCode" -> sortCode))
          result.errors shouldBe Seq(FormError("sortCode", keyLengthError))
        }
      }

      Seq("12 34 56 56", "12345678").foreach { sortCode =>
        s"not bind a sort code $sortCode which exceeds max length" in {
          val result = testForm.bind(validOtherData ++ Map("sortCode" -> sortCode))
          result.errors shouldBe Seq(FormError("sortCode", keyLengthError))
        }
      }

      "not bind a sort code which is less than the expected length" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode" -> "12345"))
        result.errors shouldBe Seq(FormError("sortCode", keyLengthError))
      }
    }
  }
}
