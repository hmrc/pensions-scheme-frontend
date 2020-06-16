/*
 * Copyright 2020 HM Revenue & Customs
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

import models.BankAccountDetails
import utils.checkyouranswers.{BankDetailsCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}
import viewmodels.Message

case object BankAccountDetailsId extends TypedIdentifier[BankAccountDetails] {
  self =>
  override def toString: String = "uKBankDetails"

  implicit def cya(implicit countryOptions: CountryOptions,
                   userAnswers: UserAnswers): CheckYourAnswers[self.type] =
    BankDetailsCYA[self.type](
      label = Some(Message("uKBankDetails.checkYourAnswersLabel", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(Message("messages__visuallyhidden__uKBankDetails", userAnswers.get(SchemeNameId)
        .getOrElse("")))
    )()
}
