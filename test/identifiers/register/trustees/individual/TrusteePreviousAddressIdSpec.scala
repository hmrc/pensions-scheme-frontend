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
import models._
import models.address.Address
import models.person.PersonName
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class TrusteePreviousAddressIdSpec extends SpecBase {

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
    val onwardUrl = "onwardUrl"
    val trusteeName = "Test Name"

    val address = Address(
      "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
    )

    def addressAnswer(address: Address): Seq[String] = {
      val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)

      Seq(
        Some(address.addressLine1),
        Some(address.addressLine2),
        address.addressLine3,
        address.addressLine4,
        address.postcode,
        Some(country)
      ).flatten
    }

    def answers: UserAnswers = UserAnswers().set(TrusteePreviousAddressId(0))(address).flatMap(
      _.set(TrusteeNameId(0))(PersonName("Test", "Name"))
    ).asOpt.value

    val answerRowWithChangeLInks = Seq(
      AnswerRow(
        Message("messages__trusteePreviousAddress", trusteeName),
        addressAnswer(address),
        answerIsMessageKey = false,
        Some(Link("site.change", onwardUrl, Some(Message("messages__changeTrusteePreviousAddress", trusteeName))))
      ))

    Seq(NormalMode, UpdateMode).foreach { mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links for subscription or variation when adding new trustee" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            answers.set(IsTrusteeNewId(0))(value = true).asOpt.value, PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          TrusteePreviousAddressId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowWithChangeLInks)
        }
      }
    }

    "in update mode" must {
      "return row with add links for existing trustee if address years is under a year and there is no previous address" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          UserAnswers().set(TrusteeAddressYearsId(0))(AddressYears.UnderAYear).flatMap(
            _.set(TrusteeNameId(0))(PersonName("Test", "Name"))).asOpt.value, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteePreviousAddressId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__trusteePreviousAddress", trusteeName),
            Seq("site.not_entered"),
            answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__changeTrusteePreviousAddress", trusteeName))))))
        )
      }

      "return row with change links for existing trustee if there is a previous address" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteePreviousAddressId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowWithChangeLInks)
      }
    }
  }
}
