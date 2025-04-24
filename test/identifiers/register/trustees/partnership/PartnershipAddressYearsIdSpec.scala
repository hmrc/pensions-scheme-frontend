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

package identifiers.register.trustees.partnership

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models.AddressYears.UnderAYear
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import models.{AddressYears, Link, NormalMode, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class PartnershipAddressYearsIdSpec extends SpecBase {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
      .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(PartnershipPreviousAddressListId(0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
      .flatMap(_.set(PartnershipHasBeenTradingId(0))(value = true))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" when {

      val result: UserAnswers = answers.set(PartnershipAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnershipPreviousAddressPostcodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(PartnershipPreviousAddressId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(PartnershipPreviousAddressListId(0)) mustNot be(defined)
      }

      "remove the data for `HasBeenTradingId`" in {
        result.get(PartnershipHasBeenTradingId(0)) mustNot be(defined)
      }
    }

    "`AddressYears` is removed" when {

      val result: UserAnswers = answers.remove(PartnershipAddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnershipPreviousAddressPostcodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnershipPreviousAddressId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(PartnershipPreviousAddressListId(0)) mustBe defined
      }

      "not remove the data for `HasBeenTradingId`" in {
        result.get(PartnershipHasBeenTradingId(0)) mustBe defined
      }
    }
  }

  "cya" when {

    val onwardUrl = "onwardUrl"

    def answers: UserAnswers = UserAnswers().set(PartnershipAddressYearsId(0))(UnderAYear).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipAddressYearsId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            Message("messages__trusteeAddressYears__heading", Message("messages__thePartnership")),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_addressYears", Message("messages__thePartnership")))))
          )))
      }
    }

    "in update mode for new trustee - company paye" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

        PartnershipAddressYearsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(
            Message("messages__trusteeAddressYears__heading", Message("messages__thePartnership")),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
             Some(Message("messages__visuallyhidden__dynamic_addressYears", Message("messages__thePartnership")))
          )))))
      }
    }

    "in update mode for existing trustee - company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        PartnershipAddressYearsId(0).row(onwardUrl, UpdateMode) must equal(Nil)
      }
    }
  }
}
