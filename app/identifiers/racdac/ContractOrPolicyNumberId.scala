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

package identifiers.racdac

import identifiers.TypedIdentifier
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.Message

case object ContractOrPolicyNumberId extends TypedIdentifier[String] {
  self =>
  override def path: JsPath = JsPath \ "racdac" \ ContractOrPolicyNumberId.toString
  override def toString: String = "contractOrPolicyNumber"
  implicit def cya(implicit countryOptions: CountryOptions, userAnswers: UserAnswers)
  : CheckYourAnswers[self.type] = {
    val racDACName = userAnswers.get(RACDACNameId).getOrElse("")
    StringCYA[self.type](
      label = Some(Message("messages__racdac_contract_or_policy_number__title", racDACName)),
      hiddenLabel = Some(Message("messages__visuallyhidden__racdac_contract_or_policy_number", racDACName))
    )()
  }
}
