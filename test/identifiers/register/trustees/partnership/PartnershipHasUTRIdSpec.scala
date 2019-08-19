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

package identifiers.register.trustees.partnership

import base.SpecBase
import identifiers.register.trustees.TrusteesId
import play.api.libs.json.Json
import utils.UserAnswers

class PartnershipHasUTRIdSpec extends SpecBase {

  import PartnershipHasUTRIdSpec._

  "cleanup" when {
    "`PartnershipHasUTR` changed to false" must {
      val result = ua(true)
        .set(PartnershipHasUTRId(0))(false)
        .asOpt.value

      "remove the data for `PartnershipUTRId`" in {
        result.get(PartnershipUTRId(0)) mustNot be(defined)
      }
    }

    "`PartnershipHasUTR` changed to true" must {
      val result = ua(false)
        .set(PartnershipHasUTRId(0))(true)
        .asOpt.value

      "remove the data for `PartnershipNoUTRReasonId`" in {
        result.get(PartnershipNoUTRReasonId(0)) mustNot be(defined)
      }
    }
  }
}

object PartnershipHasUTRIdSpec extends SpecBase {

  private def ua(v: Boolean) =
  UserAnswers(Json.obj(
    TrusteesId.toString -> Json.arr(
      Json.obj(
        PartnershipHasUTRId.toString -> v,
        PartnershipUTRId.toString -> "value",
        PartnershipNoUTRReasonId.toString -> "value"
      )
    )
  ))

}
