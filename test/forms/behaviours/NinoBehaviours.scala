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
import forms.mappings.NinoMapping
import models.Nino
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

class NinoBehaviours extends FormSpec with NinoMapping {

  def formWithNino(testForm: Form[Nino],
                   requiredKey:String = "messages__error__has_nino_director",
                   requiredNinoKey:String = "messages__error__nino" :String,
                   requiredReasonKey:String = "messages__director_no_nino" : String,
                   invalidNinoKey:String = "messages__error__nino_invalid" :String
  ): Unit = {

    "behave like a form with a NINO Mapping" should {

      "fail to bind when yes is selected but NINO is not provided" in {
        val result = testForm.bind(Map("nino.hasNino" -> "true"))
        result.errors shouldBe Seq(FormError("nino.nino", "messages__error__nino"))
      }

      "fail to bind when no is selected but reason is not provided" in {
        val result = testForm.bind(Map("nino.hasNino" -> "false"))
        result.errors shouldBe Seq(FormError("nino.reason", requiredReasonKey))
      }

      Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
        s"fail to bind when NINO $nino is invalid" in {
          val result = testForm.bind(Map("nino.hasNino" -> "true", "nino.nino" -> nino))
          result.errors shouldBe Seq(FormError("nino.nino", "messages__error__nino_invalid"))
         }
      }

      "fail to bind when no is selected and reason exceeds max length of 150" in {
        val testString = RandomStringUtils.randomAlphabetic(151)
        val result = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> testString))
        result.errors shouldBe Seq(FormError("nino.reason", "messages__error__no_nino_length", Seq(150)))
      }

      "successfully bind when yes is selected and valid NINO is provided" in {
        val form = testForm.bind(Map("nino.hasNino" -> "true", "nino.nino" -> "AB020202A"))
        form.get shouldEqual Nino.Yes("AB020202A")
      }

      "successfully bind when no is selected and reason is provided" in {
        val form = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> "haven't got Nino"))
        form.get shouldBe Nino.No("haven't got Nino")
      }

      "fail to bind when value is omitted" in {
        val expectedError = error("nino.hasNino", requiredKey)
        checkForError(testForm, emptyForm, expectedError)
      }
    }
  }
}
