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

package identifiers

import play.api.i18n.Messages
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}

case object InsurancePolicyNumberId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "insurancePolicyNumber"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[self.type] =
    StringCYA[self.type](
      label = if(userAnswers.get(InsuranceCompanyNameId).isDefined){
        Some(messages("messages__insurance_policy_number_cya_label", userAnswers.get(InsuranceCompanyNameId).getOrElse("")))
      } else {
        Some(messages("messages__insurance_policy_number_cya_label_add"))
      },
      hiddenLabel = if(userAnswers.get(InsuranceCompanyNameId).isDefined){
        Some(messages("messages__visuallyhidden__insurance_policy_number", userAnswers.get(InsuranceCompanyNameId).getOrElse("")))
      } else {
        Some(messages("messages__visuallyhidden__insurance_policy_number_add"))
      }
    )()
}
