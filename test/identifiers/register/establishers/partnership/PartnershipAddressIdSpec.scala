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

package identifiers.register.establishers.partnership

import base.SpecBase
import models.address.Address
import models.requests.DataRequest
import models.{Link, NormalMode, PartnershipDetails, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnershipAddressIdSpec extends SpecBase {

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

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

    val onwardUrl = "onwardUrl"
    Seq(NormalMode, UpdateMode).foreach { mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers().set(PartnershipAddressId(0))(address).flatMap(
              _.set(PartnershipDetailsId(0))(PartnershipDetails("test name"))).asOpt.value, PsaId("A0000000"))
          implicit val ua: UserAnswers = request.userAnswers
          PartnershipAddressId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__address__cya", "test name"),
              addressAnswer(address),
              answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_address", "test name"))))
            )))
        }
      }
    }
  }
}