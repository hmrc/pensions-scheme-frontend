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

import models.address.Address
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}
import viewmodels.Message

case object AdviserAddressId extends TypedIdentifier[Address] {
  self =>
  override def toString: String = "adviserAddress"

  implicit def cya(implicit countryOptions: CountryOptions, userAnswers: UserAnswers)
  : CheckYourAnswers[self.type] =
    AddressCYA(label = Message("adviserAddress.checkYourAnswersLabel", userAnswers.get(AdviserNameId).getOrElse("")),
      changeAddress = Message("messages__visuallyhidden__adviser__address", userAnswers.get(AdviserNameId).getOrElse
      ("")))()
}


