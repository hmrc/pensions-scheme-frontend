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

package forms.register.trustees.partnership

import forms.mappings.UtrMapping
import javax.inject.Inject
import models.UniqueTaxReference
import play.api.data.Form

class PartnershipUniqueTaxReferenceFormProvider @Inject()() extends UtrMapping {

  def apply(): Form[UniqueTaxReference] = Form(
    "uniqueTaxReference" -> uniqueTaxReferenceMapping(
      requiredKey = "messages__error__has_utr_partnership",
      requiredUtrKey = "messages__error__utr",
      requiredReasonKey = "messages__error__utr_no_utr",
      maxLengthReasonKey = "messages__error__no_utr_length",
      invalidUtrKey = "messages__error__utr_invalid",
      invalidReasonKey = "messages__error__utr_invalid"
    )
  )
}
