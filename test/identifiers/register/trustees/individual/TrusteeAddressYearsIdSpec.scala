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

package identifiers.register.trustees.individual

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models.AddressYears.UnderAYear
import models.address.{Address, TolerantAddress}
import models.person.PersonName
import models.requests.DataRequest
import models.{AddressYears, Link, NormalMode, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class TrusteeAddressYearsIdSpec extends SpecBase {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(TrusteeAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId(0))(Seq.empty))
      .flatMap(_.set(TrusteePreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(TrusteePreviousAddressListId(0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
      .asOpt.value

    "`AddressYears` is set to `UnderAYear`" when {

      val result: UserAnswers = answers.set(TrusteeAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(TrusteePreviousAddressId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(TrusteePreviousAddressListId(0)) mustNot be(defined)
      }
    }

    "`AddressYears` is removed" when {

      val result: UserAnswers = answers.remove(TrusteeAddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(TrusteePreviousAddressId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(TrusteePreviousAddressListId(0)) mustBe defined
      }
    }
  }

  "cya" when {

    val onwardUrl = "onwardUrl"
    val trusteeName = "Test Name"

    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

    def answers: UserAnswers = UserAnswers().set(TrusteeAddressYearsId(0))(UnderAYear).flatMap(
      _.set(TrusteeNameId(0))(PersonName("Test", "Name")
      )
    ).asOpt.get

    Seq(NormalMode, UpdateMode).foreach { mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links for subscription or variation when adding new trustee" in {
          val answersWithNew = answers.set(IsTrusteeNewId(0))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithNew, PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers
          TrusteeAddressYearsId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__trusteeAddressYears__heading", trusteeName),
              Seq(s"messages__common__under_a_year"),
              answerIsMessageKey = true,
              Some(Link("site.change", onwardUrl,
                Some(Message("messages__visuallyhidden__dynamic_addressYears", trusteeName))))
            )))
        }
      }
    }

    "in update mode for existing trustee" must {

      "return no answers row" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeAddressYearsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
      }
    }
  }
}
