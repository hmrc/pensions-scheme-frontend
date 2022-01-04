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

package identifiers

import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.Message

object AdviserPhoneId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "adviserPhone"

  implicit def cya(implicit countryOptions: CountryOptions, userAnswers: UserAnswers)
  : CheckYourAnswers[self.type] =
    StringCYA[self.type](
      label = Some(Message("adviserPhone.checkYourAnswersLabel", userAnswers.get(AdviserNameId).getOrElse(""))),
      hiddenLabel = Some(Message("messages__visuallyhidden__adviserPhone", userAnswers.get(AdviserNameId)
        .getOrElse("")))
    )()
}
