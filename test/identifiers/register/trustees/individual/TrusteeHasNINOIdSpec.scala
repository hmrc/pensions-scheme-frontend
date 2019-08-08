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

package identifiers.register.trustees.individual

import base.SpecBase
import models.ReferenceValue
import play.api.libs.json.Json
import utils.UserAnswers

class TrusteeHasNINOIdSpec extends SpecBase {

  "Cleanup" when {
    def answers(hasNino: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(TrusteeHasNINOId(0))(hasNino)
      .flatMap(_.set(TrusteeNewNinoId(0))(ReferenceValue("test-nino", isEditable = true)))
      .flatMap(_.set(TrusteeNoNINOReasonId(0))("reason"))
      .asOpt.value

    "`TrusteeHasNINO` is set to `false`" must {

      val result: UserAnswers = answers().set(TrusteeHasNINOId(0))(false).asOpt.value

      "remove the data for `TrusteeNino`" in {
        result.get(TrusteeNewNinoId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasNINO` is set to `true`" must {

      val result: UserAnswers = answers(false).set(TrusteeHasNINOId(0))(true).asOpt.value

      "remove the data for `TrusteeNoNinoReason`" in {
        result.get(TrusteeNoNINOReasonId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasNINO` is not present" must {

      val result: UserAnswers = answers().remove(TrusteeHasNINOId(0)).asOpt.value

      "not remove the data for `TrusteeNoNinoReason`" in {
        result.get(TrusteeNoNINOReasonId(0)) mustBe defined
      }
    }
  }
}
