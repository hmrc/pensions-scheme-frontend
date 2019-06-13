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

package forms

import com.google.inject.Inject
import forms.behaviours.VatBehaviours
import play.api.data.Form
import play.api.i18n.Messages

class VatVariationsFormProviderSpec @Inject() (implicit messages: Messages) extends VatBehaviours {

  private val vatLengthKey = "messages__error__vat_length"
  private val requiredVatKey = "messages__error__vat_required"
  private val invalidVatKey = "messages__error__vat_invalid"

  "A form with a Vat" should {
    val testForm = new VatVariationsFormProvider().apply("test company")

    behave like formWithVatVariations(testForm: Form[String],
      vatLengthKey: String,
      requiredVatKey: String,
      invalidVatKey: String
    )

  }
}
