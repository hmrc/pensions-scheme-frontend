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
import models.address.{Address, TolerantAddress}
import play.api.libs.json.Json
import utils.UserAnswers

class PartnershipHasBeenTradingIdSpec extends SpecBase {
  
  import PartnershipHasBeenTradingIdSpec._

  "cleanup" when {
    "`PartnershipHasBeenTrading` changed to false" must {
     val result = ua(true).set(PartnershipHasBeenTradingId(0))(false).asOpt.value

      "remove the data for `PartnershipPreviousAddressPostcodeLookupId`" in {
        result.get(PartnershipPreviousAddressPostcodeLookupId(0)) mustNot be(defined)
      }
      "remove the data for `PartnershipPreviousAddressId`" in {
        result.get(PartnershipPreviousAddressId(0)) mustNot be(defined)
      }
      "remove the data for `PartnershipPreviousAddressListId`" in {
        result.get(PartnershipPreviousAddressListId(0)) mustNot be(defined)
      }
    }

    "`PartnershipHasBeenTrading` changed to true" must {
      val result = ua(false).set(PartnershipHasBeenTradingId(0))(true).asOpt.value

      "remove the data for `PartnershipPreviousAddressPostcodeLookupId`" in {
        result.get(PartnershipPreviousAddressPostcodeLookupId(0)) must be(defined)
      }
      "remove the data for `PartnershipPreviousAddressId`" in {
        result.get(PartnershipPreviousAddressId(0)) must be(defined)
      }
      "remove the data for `PartnershipPreviousAddressListId`" in {
        result.get(PartnershipPreviousAddressListId(0)) must be(defined)
      }
    }
  }
}

object PartnershipHasBeenTradingIdSpec extends SpecBase {

  private def ua(v:Boolean) = UserAnswers(Json.obj())
    .set(PartnershipHasBeenTradingId(0))(v)
    .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(PartnershipPreviousAddressListId(0))(
      TolerantAddress(Some(""), Some(""), None, None, None, Some(""))))
    .asOpt
    .value
}
