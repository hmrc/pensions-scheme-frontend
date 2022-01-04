/*
 * Copyright 2022 HM Revenue & Customs
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

package identifiers

import base.SpecBase
import models.address.Address
import models.requests.DataRequest
import models.{Link, NormalMode, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class InsurerConfirmAddressIdSpec extends SpecBase {

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
    val insuranceCompany = "test insurance"

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

    val answers = UserAnswers(Json.obj("insurerAddress" -> address, "insuranceCompanyName" -> insuranceCompany))

    val onwardUrl = "onwardUrl"

    s"in normal mode" must {
      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        implicit val userAnswers: UserAnswers = request.userAnswers

        InsurerConfirmAddressId.row(onwardUrl, NormalMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__addressFor", insuranceCompany),
            addressAnswer(address),
            answerIsMessageKey = false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__insurer_confirm_address", insuranceCompany))))
          )))
      }
    }

    s"in Update mode" must {
      "return answers rows with change links when there is an address" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        implicit val userAnswers: UserAnswers = request.userAnswers

        InsurerConfirmAddressId.row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__addressFor", insuranceCompany),
            addressAnswer(address),
            answerIsMessageKey = false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__insurer_confirm_address", insuranceCompany))))
          )))
      }

      "return answers rows with add links when there is no address and benefitsSecuredByInsurance is Yes" in {
        val answers = UserAnswers(Json.obj("insuranceCompanyName" -> insuranceCompany, "securedBenefits" -> true))
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        implicit val userAnswers: UserAnswers = request.userAnswers

        InsurerConfirmAddressId.row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(
            Message("messages__addressFor", insuranceCompany),
            Seq("site.not_entered"),
            answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__insurer_confirm_address", insuranceCompany))))
          )))
      }
    }
  }
}