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
import identifiers.register.establishers.IsEstablisherNewId
import models.UniqueTaxReference._
import models._
import models.address.Address
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, InputOption, UserAnswers}
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class PreviousAddressIdSpec extends SpecBase {

  "cya" when {
    
    val onwardUrl = "onwardUrl"

    val address = Address(
      "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
    )
    implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])
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

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("previousAddress" -> address)), PsaId("A0000000"))

        PreviousAddressId(0).row(onwardUrl, NormalMode) must equal(AnswerRow(
          "messages__common__cya__address",
          addressAnswer(address),
          false,
          Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__common__address")))))
      }
    }

/*    "in update mode for new establisher - individual utr" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        UniqueTaxReferenceId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__establisher_individual_utr_question_cya_label",List("Yes"),false,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__establisher__utr_yes_no")))),
          AnswerRow("messages__establisher_individual_utr_cya_label",List("utr"),false,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__establisher__utr"))))
        ))
      }
    }

    "in update mode for existing establisher - individual utr" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        UniqueTaxReferenceId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__establisher_individual_utr_cya_label",List("utr"),false, None)
        ))
      }
    }*/
  }
}
