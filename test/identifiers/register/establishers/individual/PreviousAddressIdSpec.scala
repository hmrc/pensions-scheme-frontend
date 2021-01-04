/*
 * Copyright 2021 HM Revenue & Customs
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
import models._
import models.address.Address
import models.person.PersonName
import models.requests.DataRequest
import org.scalatest.OptionValues
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PreviousAddressIdSpec extends SpecBase {

  import PreviousAddressIdSpec._

  "cya" when {

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PreviousAddressId(index).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowWithChangeLink)
      }
    }

    "in update mode" when {
      "for new individual" must {
        "return answer row with change links" in {
          val answersNew = answers.set(IsEstablisherNewId(index))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithChangeLink)
        }
      }
      "for existing individual" must {
        "return answer row with change links if there is a previous address" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithChangeLink)
        }

        "return answer row with add link if there is no previous address and `is this previous address` is no" in {
          val answersWithNoIsThisPreviousAddress = answers.set(IndividualConfirmPreviousAddressId(index))(value = false).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithNoIsThisPreviousAddress, Some(PsaId("A0000000")))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithAddLink)
        }

        "return no answer row if there is no previous address and `is this previous address` is yes" in {
          val answersWithYesIsThisPreviousAddress = UserAnswers().set(IndividualConfirmPreviousAddressId(index))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithYesIsThisPreviousAddress, Some(PsaId("A0000000")))

          PreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
        }
      }
    }
  }
}

object PreviousAddressIdSpec extends SpecBase with OptionValues {
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

  private val onwardUrl = "onwardUrl"

  private val establisherName = PersonName("Test", "Name")

  private val answerRowWithChangeLink = Seq(
    AnswerRow(
      Message("messages__previousAddressFor", establisherName.fullName),
      addressAnswer(address),
      answerIsMessageKey = false,
      Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_previousAddress", establisherName.fullName))))
    ))

  private val answerRowWithAddLink = Seq(
    AnswerRow(
      Message("messages__previousAddressFor", establisherName.fullName),
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_previousAddress", establisherName.fullName))))
    ))

  private val answers: UserAnswers =
    UserAnswers().set(PreviousAddressId(index))(address).flatMap(
      _.set(EstablisherNameId(index))(establisherName)
    ).asOpt.value
}