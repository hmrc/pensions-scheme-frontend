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

package identifiers.register.establishers.individual

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models.AddressYears.UnderAYear
import models.address.{Address, TolerantAddress}
import models.person.PersonName
import models.requests.DataRequest
import models.{AddressYears, Link, NormalMode, UpdateMode}
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerRow, Message}

class AddressYearsIdSpec extends SpecBase with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(AddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(PreviousPostCodeLookupId(0))(Seq.empty))
      .flatMap(_.set(PreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(PreviousAddressListId(0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
      .asOpt.value

    "`AddressYears` is set to `UnderAYear`" when {

      val result: UserAnswers = answers.set(AddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(PreviousPostCodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(PreviousAddressId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(PreviousAddressListId(0)) mustNot be(defined)
      }
    }

    "`AddressYears` is set to `OverAYear`" when {

      val result: UserAnswers = UserAnswers(Json.obj())
        .set(AddressYearsId(0))(AddressYears.OverAYear)
        .flatMap(_.set(PreviousPostCodeLookupId(0))(Seq.empty))
        .flatMap(_.set(PreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
        .flatMap(_.set(PreviousAddressListId(0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
        .asOpt.value.set(AddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PreviousPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PreviousAddressId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(PreviousAddressListId(0)) mustBe defined
      }
    }

    "`AddressYears` is removed" when {

      val result: UserAnswers = answers.remove(AddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PreviousPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PreviousAddressId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(PreviousAddressListId(0)) mustBe defined
      }
    }
  }

  "cya" when {

    val onwardUrl = "onwardUrl"
    val name = "test name"
    def answers = UserAnswers().set(EstablisherNameId(0))(PersonName("test", "name")).asOpt.value
      .set(AddressYearsId(0))(UnderAYear).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        AddressYearsId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__addressYears", name),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_addressYears", name))))
          )))
      }
    }

    "in update mode for new establisher" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        AddressYearsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__addressYears", name),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_addressYears", name))))
          )))
      }
    }

    "in update mode for existing establisher" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        AddressYearsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
      }
    }
  }
}
