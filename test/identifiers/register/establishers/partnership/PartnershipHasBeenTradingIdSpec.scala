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

package identifiers.register.establishers.partnership

import base.SpecBase
import models.{Link, NormalMode, PartnershipDetails, UpdateMode}
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import viewmodels.{AnswerRow, Message}
import utils.checkyouranswers.Ops._

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

  "cya" when {
    val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers.isEstablisherNew(index, flag = true), Some(PsaId("A0000000")))

    Seq(NormalMode, UpdateMode).foreach { mode =>
      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          PartnershipHasBeenTradingId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__hasBeenTrading__h1", partnershipDetails.name),
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic__hasBeenTrading", partnershipDetails.name))))
            )))
        }
      }
    }

    "in Update Mode for an existing partnership returned from ETMP" must {
      "return no rows" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipHasBeenTradingId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
      }
    }
  }

}

object PartnershipHasBeenTradingIdSpec extends SpecBase with OptionValues {
  private val index = 0
  private val partnershipDetails = PartnershipDetails("test partnership name")

  private def ua(v: Boolean): UserAnswers = UserAnswers(Json.obj())
    .set(PartnershipHasBeenTradingId(0))(v)
    .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(PartnershipPreviousAddressListId(0))(
      TolerantAddress(Some(""), Some(""), None, None, None, Some(""))))
    .asOpt
    .value

  private val onwardUrl = "onwardUrl"
  private val answers = UserAnswers().set(PartnershipDetailsId(index))(partnershipDetails).asOpt.value.
    set(PartnershipHasBeenTradingId(index))(value = false).asOpt.value
}
