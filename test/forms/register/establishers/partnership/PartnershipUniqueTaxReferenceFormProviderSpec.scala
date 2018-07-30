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

package forms.register.establishers.partnership

import forms.behaviours.UtrBehaviour

class PartnershipUniqueTaxReferenceFormProviderSpec extends UtrBehaviour {

  val requiredKey = "messages__error__has_utr_establisher_partnership"
  val requiredUtrKey = "messages__error__utr"
  val requiredReasonKey = "messages__error__utr_no_utr_establisher"
  val invalidUtrKey = "messages__error__utr_invalid"
  val maxLengthReasonKey = "messages__error__no_utr_length"
  val invalidReasonKey = "messages__error__utr_invalid"

  val formProvider = new PartnershipUniqueTaxReferenceFormProvider()

  "CompanyUniqueTaxReference form" must {

    behave like formWithUtr(
      formProvider(),
      requiredKey,
      requiredUtrKey,
      requiredReasonKey,
      invalidUtrKey,
      maxLengthReasonKey,
      invalidReasonKey
    )
  }
}

