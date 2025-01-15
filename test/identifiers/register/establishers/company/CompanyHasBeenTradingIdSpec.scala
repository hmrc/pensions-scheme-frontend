/*
 * Copyright 2024 HM Revenue & Customs
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

package identifiers.register.establishers.company

import base.SpecBase
import models._
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class CompanyHasBeenTradingIdSpec extends SpecBase {
  import CompanyHasBeenTradingIdSpec._

  "cleanup" when {
    "`CompanyHasBeenTrading` changed to false" must {
      val result = ua(true).set(HasBeenTradingCompanyId(0))(false).asOpt.value

      "remove the data for `CompanyPreviousAddressPostcodeLookupId`" in {
        result.get(CompanyPreviousAddressPostcodeLookupId(0)) mustNot be(defined)
      }
      "remove the data for `CompanyPreviousAddressId`" in {
        result.get(CompanyPreviousAddressId(0)) mustNot be(defined)
      }
      "remove the data for `CompanyPreviousAddressListId`" in {
        result.get(CompanyPreviousAddressListId(0)) mustNot be(defined)
      }
    }

    "`CompanyHasBeenTrading` changed to true" must {
      val result = ua(false).set(HasBeenTradingCompanyId(0))(true).asOpt.value

      "remove the data for `CompanyPreviousAddressPostcodeLookupId`" in {
        result.get(CompanyPreviousAddressPostcodeLookupId(0)) must be(defined)
      }
      "remove the data for `CompanyPreviousAddressId`" in {
        result.get(CompanyPreviousAddressId(0)) must be(defined)
      }
      "remove the data for `CompanyPreviousAddressListId`" in {
        result.get(CompanyPreviousAddressListId(0)) must be(defined)
      }
    }
  }

  "cya" when {
    val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers.isEstablisherNew(Index(0), flag = true), Some(PsaId("A0000000")))

    Seq(NormalMode, UpdateMode).foreach { mode =>
      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          HasBeenTradingCompanyId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__hasBeenTrading__h1", companyDetails.companyName),
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic__hasBeenTrading", companyDetails.companyName))))
            )))
        }
      }
    }

    "in Update Mode for an existing Company returned from ETMP" must {
      "return no rows" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        HasBeenTradingCompanyId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
      }
    }
  }
}

object CompanyHasBeenTradingIdSpec extends SpecBase {
  private val index = 0
  private val companyDetails = CompanyDetails("test company name")

  private def ua(v: Boolean): UserAnswers = UserAnswers(Json.obj())
    .set(HasBeenTradingCompanyId(0))(v)
    .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyPreviousAddressListId(0))(
      TolerantAddress(Some(""), Some(""), None, None, None, Some(""))))
    .asOpt
    .value

  private val onwardUrl = "onwardUrl"
  private val answers = UserAnswers().establisherCompanyDetails(Index(0), companyDetails).
    establisherCompanyTradingTime(Index(0), hasBeenTrading = false)
}