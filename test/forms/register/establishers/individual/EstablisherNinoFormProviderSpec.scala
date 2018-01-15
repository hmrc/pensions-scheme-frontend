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

import forms.behaviours.FormBehaviours
import models.{Field, Invalid, Required, EstablisherNino}

class EstablisherNinoFormProviderSpec extends FormBehaviours {

  val requiredKey = "messages__error__has_nino_establisher"
  val requiredNinoKey = "messages__error__nino"
  val requiredReasonKey = "messages__establisher__no_nino"
  val invalidNinoKey = "messages__error__nino_invalid"

  val formProvider = new EstablisherNinoFormProvider()

  "EstablisherNino form provider" must {

    "successfully bind when yes is selected and valid NINO is provided" in {
      val form = formProvider().bind(Map("establisherNino.hasNino" -> "yes", "establisherNino.nino" -> "AB020202A"))
      form.get shouldBe EstablisherNino.Yes("AB020202A")
    }

    "successfully bind when no is selected and reason is provided" in {
      val form = formProvider().bind(Map("establisherNino." -> "no", "establisherNino.reason" -> "Reason"))
      form.get shouldBe EstablisherNino.No("Reason")
    }

    "fail to bind when yes is selected and nothing is provided" in {
      val form = formProvider().bind(Map("establisherNino." -> "yes", "establisherNino.nino" -> ""))
      
    }

    behave like questionForm[EstablisherNino](EstablisherNino)

    behave like formWithOptionField(
      Field(
        "value",
        Required -> "messages__error__has_nino_establisher",
        Invalid -> "error.invalid"),
      EstablisherNino.options.map(_.value): _*)
  }
}
