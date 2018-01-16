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

package forms.register.establishers.individual

import forms.FormSpec
import models.EstablisherNino

class EstablisherNinoFormProviderSpec extends FormSpec {

  val requiredKey = "messages__error__has_nino_establisher"
  val requiredNinoKey = "messages__error__nino"
  val requiredReasonKey = "messages__establisher__no_nino"
  val invalidNinoKey = "messages__error__nino_invalid"

  val formProvider = new EstablisherNinoFormProvider()()
  val validData:Map[String,String] = Map(
    "establisherNino.hasNino" ->"true",
    "establisherNino.nino" -> "AB020202A"
  )

  "EstablisherNino form provider" must {

    "successfully bind when yes is selected and valid NINO is provided" in {
      val form = formProvider.bind(Map("establisherNino.hasNino" -> "true", "establisherNino.nino" -> "AB020202A"))
      form.get shouldBe EstablisherNino.Yes("AB020202A")
    }

    "successfully bind when no is selected and reason is provided" in {
      val form = formProvider.bind(Map("establisherNino.hasNino" -> "false", "establisherNino.reason" -> "haven't got Nino"))
      form.get shouldBe EstablisherNino.No("haven't got Nino")
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("establisherNino.hasNino", requiredKey)
      checkForError(formProvider, emptyForm, expectedError)
    }

    "fail to bind when yes is selected but NINO is not provided" in {
      val expectedError = error("establisherNino.nino", requiredNinoKey)
      checkForError(formProvider, Map("establisherNino.hasNino" -> "true"), expectedError)
    }

    "fail to bind when no is selected but reason is not provided" in {
      val expectedError = error("establisherNino.reason", requiredReasonKey)
      checkForError(formProvider, Map("establisherNino.hasNino" -> "false"), expectedError)
    }

    Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
      s"fail to bind when NINO $nino is invalid" in {
        val data = validData + ("establisherNino.nino" -> nino)
        val expectedError = error("establisherNino.nino", invalidNinoKey)
        checkForError(formProvider, data, expectedError)
      }
    }
  }
}
