/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError

class ContractOrPolicyNumberFormProviderSpec extends StringFieldBehaviours with Constraints with SpecBase {
  private val racDACName = "RAC dac name"
  val validData: Map[String, String] = Map(
    "value" -> "racDAC Number 1")
  val validMaxLength = 50
  val formProvider = new ContractOrPolicyNumberFormProvider
  private val form = formProvider(racDACName)

  "ContractOrPolicyNumberFormProvider" must {
    val fieldName = "value"
    val lengthKey = "messages__error__racdac_contract_or_policy_number_length"
    val requiredKey = "messages__error__racdac_contract_or_policy_number"

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = validMaxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(validMaxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, messages(requiredKey, racDACName))
    )
  }
}
