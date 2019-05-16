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

package identifiers.register.establishers.partnership.partner

import identifiers.register.establishers.IsEstablisherCompleteId
import models.{AddressYears, Link, NormalMode, UpdateMode}
import models.AddressYears.UnderAYear
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{Enumerable, UserAnswers}
import viewmodels.AnswerRow
import utils.checkyouranswers.Ops._

class PartnerAddressYearsIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(PartnerAddressYearsId(0, 0))(AddressYears.UnderAYear)
      .flatMap(_.set(PartnerPreviousAddressPostcodeLookupId(0, 0))(Seq.empty))
      .flatMap(_.set(PartnerPreviousAddressId(0, 0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(PartnerPreviousAddressListId(0, 0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
      .flatMap(_.set(IsPartnerCompleteId(0, 0))(true))
      .flatMap(_.set(IsEstablisherCompleteId(0))(true))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" when {

      val result: UserAnswers = answers.set(PartnerAddressYearsId(0, 0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostcodeLookupId(0, 0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0, 0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(PartnerPreviousAddressListId(0, 0)) mustNot be(defined)
      }

      "do not change the value of IsPartnerCompleteId and IsEstablisherCompleteId" in {
        result.get(IsPartnerCompleteId(0, 0)).value mustBe true
        result.get(IsEstablisherCompleteId(0)).value mustBe true
      }
    }

    "`AddressYears` is set to `UnderAYear`" when {

      val result: UserAnswers = UserAnswers(Json.obj())
        .set(PartnerAddressYearsId(0, 0))(AddressYears.OverAYear)
        .flatMap(_.set(PartnerPreviousAddressPostcodeLookupId(0, 0))(Seq.empty))
        .flatMap(_.set(PartnerPreviousAddressId(0, 0))(Address("foo", "bar", None, None, None, "GB")))
        .flatMap(_.set(PartnerPreviousAddressListId(0, 0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
        .flatMap(_.set(IsPartnerCompleteId(0, 0))(true))
        .flatMap(_.set(IsEstablisherCompleteId(0))(true))
        .asOpt.value.set(PartnerAddressYearsId(0, 0))(AddressYears.UnderAYear).asOpt.value

      "set the IsPartnerCompleteId and IsEstablisherCompleteId to false" in {
        result.get(IsPartnerCompleteId(0, 0)).value mustBe false
        result.get(IsEstablisherCompleteId(0)).value mustBe false
      }

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostcodeLookupId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(PartnerPreviousAddressListId(0, 0)) mustBe defined
      }
    }

    "`AddressYears` is removed" when {

      val result: UserAnswers = answers.remove(PartnerAddressYearsId(0, 0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostcodeLookupId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(PartnerPreviousAddressListId(0, 0)) mustBe defined
      }
    }
  }

  "cya" when {

    val onwardUrl = "onwardUrl"

    def answers = UserAnswers().set(PartnerAddressYearsId(0, 0))(UnderAYear).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        PartnerAddressYearsId(0, 0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            "messages__partner_address_years_question_cya_label",
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some("messages__visuallyhidden__partner__address_years")))
          )))
      }
    }

    "in update mode for new trustee - company paye" must {

      def answersNew: UserAnswers = answers.set(IsNewPartnerId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        PartnerAddressYearsId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(
            "messages__partner_address_years_question_cya_label",
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some("messages__visuallyhidden__partner__address_years")))
          )))
      }
    }

    "in update mode for existing trustee - company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        PartnerAddressYearsId(0, 0).row(onwardUrl, UpdateMode) must equal(Nil)
      }
    }
  }
}
