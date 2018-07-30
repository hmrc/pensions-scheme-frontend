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

package forms

import forms.behaviours.VatBehaviours
import models.Vat
import play.api.data.Form

class VatFormProviderSpec extends VatBehaviours {

  private val requiredKey = "messages__error__has_vat_establisher"
  private val requiredVatKey = "messages__error__vat_required"
  private val vatLengthKey = "messages__error__vat_length"
  private val invalidVatKey = "messages__error__vat_invalid"

  "A form with a Vat" should {
    val mapping = vatMapping(
      requiredKey,
      vatLengthKey,
      requiredVatKey,
      invalidVatKey
    )

    val testForm = new VatFormProvider().apply()

    behave like formWithVat(testForm: Form[Vat],
      requiredKey: String,
      vatLengthKey: String,
      requiredVatKey: String,
      invalidVatKey: String
    )

  }
}
