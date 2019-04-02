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

package identifiers.register.establishers.company

import models.address.Address
import models.requests.DataRequest
import models.{Link, UpdateMode}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, InputOption, UserAnswers}
import viewmodels.AnswerRow

class CompanyPreviousAddressIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "cya" must {
    val address = Address("address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB")
    val answers = UserAnswers().set(CompanyPreviousAddressId(0))(address).asOpt.value
    val onwardUrl = controllers.routes.IndexController.onPageLoad().url

    implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])
    implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))

    "produce rows with change link for previous address" when {
      "in Normal Mode " in {
        CompanyPreviousAddressId(0).row(onwardUrl) must equal(Seq(
          AnswerRow(
            "messages__common__cya__previous_address",
            answers.addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__establisher__previous_address")))
          )))
      }
    }
    "produce rows without change link for previous address" when {
      "in Update Mode " in {
        CompanyPreviousAddressId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(
            "messages__common__cya__previous_address",
            answers.addressAnswer(address),
            false,
            None
          )))
      }
    }
  }
}
