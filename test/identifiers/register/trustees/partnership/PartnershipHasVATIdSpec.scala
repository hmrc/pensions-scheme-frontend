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

class PartnershipHasVATIdSpec extends SpecBase {

  import PartnershipHasVATIdSpec._

  "cleanup" when {
    "`PartnershipHasVAT` changed to false" must {
      val result = ua
        .set(PartnershipHasVATId(0))(false)
        .asOpt.value

      "remove the data for `PartnershipVatVariationsId`" in {
        result.get(PartnershipVatVariationsId(0)) mustNot be(defined)
      }
    }
  }
}

object PartnershipHasVATIdSpec extends SpecBase {

  private val ua =
    UserAnswers(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          PartnershipHasVATId.toString -> true,
          PartnershipVatVariationsId.toString -> "value"
        )
      )
    ))

}

