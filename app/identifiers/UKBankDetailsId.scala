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

import models.register.UKBankDetails
import play.api.i18n.Messages
import utils.checkyouranswers.{BankDetailsHnSCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}

case object UKBankDetailsId extends TypedIdentifier[UKBankDetails] {
  self =>
  override def toString: String = "uKBankDetails"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages,
                   userAnswers: UserAnswers): CheckYourAnswers[self.type] =
    BankDetailsHnSCYA[self.type](
      label = Some(messages("uKBankDetails.hns_checkYourAnswersLabel", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(messages("messages__visuallyhidden__hns_uKBankDetails", userAnswers.get(SchemeNameId).getOrElse("")))
    )()
}
