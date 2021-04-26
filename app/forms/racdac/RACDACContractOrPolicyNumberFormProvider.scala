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
import play.api.i18n.Messages

class RACDACContractOrPolicyNumberFormProvider extends Mappings with Constraints {
  private val maxLength = 50

  def apply(name: String)(implicit messages: Messages): Form[String] = Form(
    "racDACContractOrPolicyNumber" -> text(messages("messages__error__racdac_contract_or_policy_number", name)).
      verifying(firstError(
        maxLength(maxLength, "messages__error__racdac_contract_or_policy_number_length")))
  )
}
