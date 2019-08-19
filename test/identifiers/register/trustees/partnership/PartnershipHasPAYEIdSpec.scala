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
import models.ReferenceValue
import play.api.libs.json.Json
import utils.UserAnswers

class PartnershipHasPAYEIdSpec extends SpecBase {
  
  import PartnershipHasPAYEIdSpec._

  "cleanup" when {
    "`PartnershipHasPAYE` changed to false" must {
     val result = ua(true).set(PartnershipHasPAYEId(0))(false).asOpt.value

      "remove the data for `PartnershipPayeVariationsId`" in {
        result.get(PartnershipPayeVariationsId(0)) mustNot be(defined)
      }
    }

    "`PartnershipHasPAYE` changed to true" must {
      val result = ua(false).set(PartnershipHasPAYEId(0))(true).asOpt.value

      "not remove the data for `PartnershipPayeVariationsId`" in {
        result.get(PartnershipPayeVariationsId(0)) must be(defined)
      }
    }
  }
}

object PartnershipHasPAYEIdSpec extends SpecBase {

  private def ua(v:Boolean) = UserAnswers(Json.obj())
    .set(PartnershipHasPAYEId(0))(v)
    .flatMap(_.set(PartnershipPayeVariationsId(0))(ReferenceValue("value")))
    .asOpt
    .value
}

