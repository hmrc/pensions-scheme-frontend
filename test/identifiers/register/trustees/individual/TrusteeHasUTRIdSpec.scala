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

class TrusteeHasUTRIdSpec extends SpecBase {

  "Cleanup" when {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(TrusteeHasUTRId(0))(hasUtr)
      .flatMap(_.set(TrusteeUTRId(0))(ReferenceValue("test-utr")))
      .flatMap(_.set(TrusteeNoUTRReasonId(0))("reason"))
      .asOpt.value

    "`TrusteeHasUTR` is set to `false`" must {

      val result: UserAnswers = answers().set(TrusteeHasUTRId(0))(false).asOpt.value

      "remove the data for `TrusteeUTR`" in {
        result.get(TrusteeUTRId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasUTR` is set to `true`" must {

      val result: UserAnswers = answers(false).set(TrusteeHasUTRId(0))(true).asOpt.value

      "remove the data for `NoUTRReason`" in {
        result.get(TrusteeNoUTRReasonId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasUTR` is not present" must {

      val result: UserAnswers = answers().remove(TrusteeHasUTRId(0)).asOpt.value

      "not remove the data for `TrusteeUTR`" in {
        result.get(TrusteeUTRId(0)) mustBe defined
      }

      "not remove the data for `NoUTRReason`" in {
        result.get(TrusteeNoUTRReasonId(0)) mustBe defined
      }
    }
  }
}
