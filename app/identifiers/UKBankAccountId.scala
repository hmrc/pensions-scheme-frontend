/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.JsResult
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.Message

case object UKBankAccountId extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "uKBankAccount"

  implicit def cya(implicit userAnswers: UserAnswers): CheckYourAnswers[self.type] =
    BooleanCYA[self.type](
      label = Some(Message("uKBankAccount.checkYourAnswersLabel", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(Message("messages__visuallyhidden__uKBankAccount", userAnswers.get(SchemeNameId)
        .getOrElse("")))
    )()

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) => userAnswers.remove(BankAccountDetailsId)
      case _ => super.cleanup(value, userAnswers)
    }
  }
}
