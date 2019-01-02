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

package forms.register.trustees.individual

import forms.mappings.NinoMapping
import models.Nino
import play.api.data.Form

class TrusteeNinoFormProvider extends NinoMapping {

  import TrusteeNinoFormProvider._

  def apply(): Form[Nino] = Form(
    "nino" -> ninoMapping(
      requiredKey,
      requiredNinoKey,
      requiredReasonKey,
      reasonLengthKey,
      invalidNinoKey,
      invalidReasonKey
    )
  )

}

object TrusteeNinoFormProvider {

  val requiredKey = "messages__trusteeNino__error__required"
  val requiredNinoKey = "messages__error__nino"
  val requiredReasonKey = "messages__trusteeNino__error__nino_reason_required"
  val reasonLengthKey: String = "messages__error__no_nino_length"
  val invalidNinoKey = "messages__error__nino_invalid"
  val invalidReasonKey = "messages__error__no_nino_invalid"
}
