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

package identifiers.register.establishers.individual

import base.SpecBase
import config.FeatureSwitchManagementService
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.address.Address
import models.person.PersonName
import models.requests.DataRequest
import org.scalatest.OptionValues
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, FakeFeatureSwitchManagementService, InputOption, UserAnswers}
import viewmodels.AnswerRow

class PreviousAddressIdSpec extends SpecBase {

  import PreviousAddressIdSpec._

  implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])
  implicit val featureSwitchManagementService: FeatureSwitchManagementService = new FakeFeatureSwitchManagementService(true)

  val name = "test name"

  val address = Address(
    "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
  )

  private val answerRowWithChangeLink = Seq(
    AnswerRow(
      "messages__establisher_individual_previous_address_cya_label",
      addressAnswer(address),
      answerIsMessageKey = false,
      Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__establisher__previous_address")))
    ))

  private val answerRowWithAddLink = Seq(
    AnswerRow("messages__establisher_individual_previous_address_cya_label",
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", onwardUrl, Some("messages__visuallyhidden__establisher__previous_address")))))

  "cya" when {

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))

        PreviousAddressId(index).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowWithChangeLink)
      }
    }

    "in update mode" when {
      "for new individual" must {
        "return answer row with change links" in {
          val answersNew = answers.set(IsEstablisherNewId(index))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithChangeLink)
        }
      }
      "for existing individual" must {
        "return answer row with change links if there is a previous address" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithChangeLink)
        }

        "return answer row with add link if there is no previous address and `is this previous address` is no" in {
          val answersWithNoIsThisPreviousAddress = UserAnswers().set(IndividualConfirmPreviousAddressId(index))(value = false).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithNoIsThisPreviousAddress, PsaId("A0000000"))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithAddLink)
        }

        "return no answer row if there is no previous address and `is this previous address` is yes" in {
          val answersWithYesIsThisPreviousAddress = UserAnswers().set(IndividualConfirmPreviousAddressId(index))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithYesIsThisPreviousAddress, PsaId("A0000000"))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
        }
      }
    }
  }
}

object PreviousAddressIdSpec extends OptionValues {
  private val index = 0
  implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
  private val address = Address(
    "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
  )

  private def addressAnswer(address: Address): Seq[String] = {
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

    val answers = UserAnswers().set(EstablisherNameId(0))(PersonName("test", "name")).asOpt.value

    val onwardUrl = "onwardUrl"
    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          answers.set(PreviousAddressId(0))(address).asOpt.value, PsaId("A0000000"))
        implicit val ua: UserAnswers = request.userAnswers

        PreviousAddressId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            messages("messages__previousAddressFor", name),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))
          )))
      }
    }

    "in update mode" must {
      "return row with add links for existing establisher if address years is under a year and there is no previous address" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          answers.set(AddressYearsId(0))(UnderAYear).asOpt.value, PsaId("A0000000"))
        implicit val ua: UserAnswers = request.userAnswers

        PreviousAddressId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(messages("messages__previousAddressFor", name),
            Seq("site.not_entered"),
            answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))))
        )
      }

      "return row with change links for existing establisher if there is a previous address" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          answers.set(PreviousAddressId(0))(address).asOpt.value, PsaId("A0000000"))
        implicit val ua: UserAnswers = request.userAnswers

        PreviousAddressId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            messages("messages__previousAddressFor", name),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))
          )))
      }

      "return row with change links for new establisher if there is a previous address" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          answers.set(PreviousAddressId(0))(address).flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value, PsaId("A0000000"))
        implicit val ua: UserAnswers = request.userAnswers

        PreviousAddressId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            messages("messages__previousAddressFor", name),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_previousAddress", name))))
          )))
      }
    }
  }
}