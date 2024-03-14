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

package identifiers.register.establishers.partnership

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models.AddressYears.UnderAYear
import models._
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import org.scalatest.{ OptionValues}
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnershipAddressYearsIdSpec extends SpecBase with Matchers with OptionValues with Enumerable.Implicits {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
      .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(PartnershipPreviousAddressListId(0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
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
    }

    "`AddressYears` is set to `UnderAYear`" when {

      val result: UserAnswers = UserAnswers(Json.obj())
        .set(PartnershipAddressYearsId(0))(AddressYears.OverAYear)
        .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
        .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
        .flatMap(_.set(PartnershipPreviousAddressListId(0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
        .asOpt.value.set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnershipPreviousAddressPostcodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnershipPreviousAddressId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(PartnershipPreviousAddressListId(0)) mustBe defined
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
    }
  }

  "cya" when {

    val onwardUrl = "onwardUrl"
    val name = "test partnership name"

    def answers: UserAnswers = UserAnswers().set(PartnershipDetailsId(0))(PartnershipDetails(name)).asOpt.value
      .set(PartnershipAddressYearsId(0))(UnderAYear).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipAddressYearsId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__partnershipAddressYears__heading", name),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_addressYears", name))))
          )))
      }
    }

    "in update mode for new trustee - company paye" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

        PartnershipAddressYearsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__partnershipAddressYears__heading", name),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_addressYears", name))))
          )))
      }
    }

    "in update mode for existing trustee - company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        PartnershipAddressYearsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
      }
    }
  }
}
