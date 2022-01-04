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

import models.Members
import play.api.libs.json.Reads
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.MembersCYA
import viewmodels.Message

case object CurrentMembersId extends TypedIdentifier[Members] {
  self =>
  override def toString: String = "membership"

  implicit def cya(implicit userAnswers: UserAnswers, rds: Reads[Members]): CheckYourAnswers[self
    .type] =
    MembersCYA[self.type](
      label = Some(Message("messages__current_members_cya_label", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(Message("messages__visuallyhidden__current_members_change", userAnswers.get(SchemeNameId)
        .getOrElse("")))
    )()
}
