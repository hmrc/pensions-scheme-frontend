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

package identifiers.register.establishers.company.director

import base.SpecBase
import models.AddressYears.UnderAYear
import models.{Link, NormalMode, UpdateMode}
import models.address.Address
import models.person.PersonName
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.AnswerRow
import utils.checkyouranswers.Ops._

class DirectorPreviousAddressIdSpec extends SpecBase {

  private val userAnswersWithName: UserAnswers =
    UserAnswers()
      .set(DirectorNameId(0, 0))(PersonName("first", "last"))
      .asOpt
      .value
  
  private val name = "first last"

  "cya" when {
    implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])

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
    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          userAnswersWithName.set(DirectorPreviousAddressId(0, 0))(address).asOpt.value, PsaId("A0000000"))

        DirectorPreviousAddressId(0, 0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            messages("messages__common__cya__previous_address"),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))
          )))
      }
    }

    "in update mode" must {
      "return row with add links for existing establisher if address years is under a year and there is no previous address" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          userAnswersWithName.set(DirectorAddressYearsId(0, 0))(UnderAYear).asOpt.value, PsaId("A0000000"))

        DirectorPreviousAddressId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(messages("messages__common__cya__previous_address"),
            Seq("site.not_entered"),
            answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))))
        )
      }

      "return row with change links for existing establisher if there is a previous address" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          userAnswersWithName.set(DirectorPreviousAddressId(0, 0))(address).asOpt.value, PsaId("A0000000"))

        DirectorPreviousAddressId(0, 0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            messages("messages__common__cya__previous_address"),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))
          )))
      }

      "return row with change links for new establisher if there is a previous address" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          userAnswersWithName.set(DirectorPreviousAddressId(0, 0))(address).flatMap(_.set(IsNewDirectorId(0, 0))(true)).asOpt.value, PsaId("A0000000"))

        DirectorPreviousAddressId(0, 0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            messages("messages__common__cya__previous_address"),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))
          )))
      }
    }
  }
}
