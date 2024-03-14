/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models.address.Address
import models.requests.DataRequest
import models.{CompanyDetails, Link, NormalMode, UpdateMode}
import org.scalatest.OptionValues
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class CompanyPreviousAddressIdSpec extends SpecBase {

  import CompanyPreviousAddressIdSpec._

  private val answerRowWithChangeLink = Seq(
    AnswerRow(
      Message("messages__previousAddress__cya", companyDetails.companyName),
      addressAnswer(address),
      answerIsMessageKey = false,
      Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_previousAddress", companyDetails.companyName))))
    ))

  private val answerRowWithAddLink = Seq(
    AnswerRow(Message("messages__previousAddress__cya", companyDetails.companyName),
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_previousAddress", companyDetails.companyName))))))

  "cya" when {

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        CompanyPreviousAddressId(index).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowWithChangeLink)
      }
    }

    "in update mode" when {
      "for new company" must {
        "return answer row with change links" in {
          val answersNew = answers.set(IsEstablisherNewId(index))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

          CompanyPreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithChangeLink)
        }
      }
      "for existing company" must {
        "return answer row with change links if there is a previous address" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

          CompanyPreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithChangeLink)
        }

        "return answer row with add link if there is no previous address and `is this previous address` is no" in {
          val answersWithNoIsThisPreviousAddress = UserAnswers().establisherCompanyDetails(index, companyDetails).
            set(CompanyConfirmPreviousAddressId(index))(value = false).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithNoIsThisPreviousAddress, Some(PsaId("A0000000")))

          CompanyPreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowWithAddLink)
        }

        "return no answer row if there is no previous address and `is this previous address` is yes" in {
          val answersWithYesIsThisPreviousAddress = UserAnswers().establisherCompanyDetails(index, companyDetails).
            set(CompanyConfirmPreviousAddressId(index))(value = true).asOpt.value
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersWithYesIsThisPreviousAddress, Some(PsaId("A0000000")))

          CompanyPreviousAddressId(index).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
        }
      }
    }
  }
}

object CompanyPreviousAddressIdSpec extends OptionValues {
  private val index = 0
  implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
  private val companyDetails = CompanyDetails("test company")
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

  private val answers: UserAnswers = UserAnswers().set(CompanyPreviousAddressId(index))(address).flatMap(
    _.set(CompanyDetailsId(index))(companyDetails)).asOpt.value
}