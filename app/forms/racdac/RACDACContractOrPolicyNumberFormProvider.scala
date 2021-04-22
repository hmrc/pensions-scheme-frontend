/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.racdac

import forms.mappings.{Constraints, Mappings}
import javax.inject.Inject
import play.api.data.Form

class RACDACContractOrPolicyNumberFormProvider @Inject() extends Mappings with Constraints {
  val schemeNameMaxLength = 160

  def apply(): Form[String] = Form(
    "racDACContractOrPolicyNumber" -> text("messages__error__racdac_contract_or_policy_number").
      verifying(firstError(
        maxLength(schemeNameMaxLength, "messages__error__racdac_contract_or_policy_number_length"),
        safeText("messages__error__racdac_contract_or_policy_number_invalid")))
  )
}
