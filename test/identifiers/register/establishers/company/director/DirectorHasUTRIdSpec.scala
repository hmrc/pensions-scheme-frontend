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

package identifiers.register.establishers.company.director

import base.SpecBase
import models.ReferenceValue
import play.api.libs.json.Json
import utils.UserAnswers

class DirectorHasUTRIdSpec extends SpecBase {

  "Cleanup" when {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(DirectorHasUTRId(0, 0))(hasUtr)
      .flatMap(_.set(DirectorEnterUTRId(0, 0))(ReferenceValue("test-utr")))
      .flatMap(_.set(DirectorNoUTRReasonId(0, 0))("reason"))
      .asOpt.value

    "`DirectorHasUTR` is set to `false`" must {

      val result: UserAnswers = answers().set(DirectorHasUTRId(0, 0))(false).asOpt.value

      "remove the data for `DirectorUTR`" in {
        result.get(DirectorEnterUTRId(0, 0)) mustNot be(defined)
      }
    }

    "`DirectorHasUTR` is set to `true`" must {

      val result: UserAnswers = answers(false).set(DirectorHasUTRId(0, 0))(true).asOpt.value

      "remove the data for `NoUTRReason`" in {
        result.get(DirectorNoUTRReasonId(0, 0)) mustNot be(defined)
      }
    }

    "`DirectorHasUTR` is not present" must {

      val result: UserAnswers = answers().remove(DirectorHasUTRId(0, 0)).asOpt.value

      "not remove the data for `DirectorUTR`" in {
        result.get(DirectorEnterUTRId(0, 0)) mustBe defined
      }

      "not remove the data for `NoUTRReason`" in {
        result.get(DirectorNoUTRReasonId(0, 0)) mustBe defined
      }
    }
  }

}
