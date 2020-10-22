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

package identifiers

import base.SpecBase
import models.address.Address
import models.requests.DataRequest
import models.{Link, Mode, NormalMode, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class AdviserAddressIdSpec extends SpecBase with Enumerable.Implicits {

  val name = "adviserName"
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

      s"in ${Mode.jsLiteral.to(mode)} mode" must {
        "return answers rows with change links" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers().set(AdviserAddressId)(address).flatMap(
              _.set(AdviserNameId)(name)).asOpt.value, Some(PsaId("A0000000")))

          implicit val ua: UserAnswers = request.userAnswers

          AdviserAddressId.row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("adviserAddress.checkYourAnswersLabel", name),
              addressAnswer(address),
              answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__adviser__address", name))))
            )))
        }
      }
    }
  }
}
