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

import forms.mappings.VatMapping
import javax.inject.Inject
import models.Vat
import play.api.data.Form

class VatFormProvider @Inject() extends VatMapping {

  def apply(requiredKeyMsg: String = "messages__error__has_vat_establisher"): Form[Vat] =
    Form(
      "vat" -> vatMapping(
        requiredKey = requiredKeyMsg,
        vatLengthKey = "messages__error__vat_length"
      )
    )
}
