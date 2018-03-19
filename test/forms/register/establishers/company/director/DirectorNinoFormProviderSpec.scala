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

package forms.register.establishers.company.director

import forms.FormSpec
import forms.behaviours.NinoBehaviours
import models.Nino

class DirectorNinoFormProviderSpec extends NinoBehaviours {

  val requiredKey = "messages__error__has_nino_director"
  val requiredNinoKey = "messages__error__nino"
  val requiredReasonKey = "messages__director_no_nino"
  val invalidNinoKey = "messages__error__nino_invalid"
  val testForm = new DirectorNinoFormProvider().apply()

    "DirectorNinoFormProviderSpec" should {


      behave like formWithNino(testForm,
      requiredKey,
      requiredNinoKey,
      requiredReasonKey,
      invalidNinoKey)
    }
//
//    "successfully bind when yes is selected and valid NINO is provided" in {
//      val form = testForm.bind(Map("nino.hasNino" -> "true", "nino.nino" -> "AB020202A"))
//      form.get shouldEqual Nino.Yes("AB020202A")
//    }
//
//    "successfully bind when no is selected and reason is provided" in {
//      val form = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> "haven't got Nino"))
//      form.get shouldBe Nino.No("haven't got Nino")
//    }

//    "fail to bind when value is omitted" in {
//      val expectedError = error("nino.hasNino", requiredKey)
//      checkForError(testForm, emptyForm, expectedError)
//    }

}
