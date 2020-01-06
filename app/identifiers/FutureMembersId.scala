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

import models.Members
import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.MembersCYA

case object FutureMembersId extends TypedIdentifier[Members] {
  self =>
  override def toString: String = "membershipFuture"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, rds: Reads[Members]): CheckYourAnswers[self.type] =
    MembersCYA[self.type](
      label = Some(messages("messages__future_members_cya_label", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(messages("messages__visuallyhidden__future_members_change", userAnswers.get(SchemeNameId).getOrElse("")))
    )()
}
