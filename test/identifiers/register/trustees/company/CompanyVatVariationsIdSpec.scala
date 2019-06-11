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

package identifiers.register.trustees.company

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.AnswerRow

class CompanyVatVariationsIdSpec extends SpecBase {

  "cya" when {

    val onwardUrl = "onwardUrl"
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
    implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
      UserAnswers().set(CompanyVatVariationsId(0))("test-vat").asOpt.get, PsaId("A0000000"))
    implicit val userAnswers: UserAnswers = request.userAnswers

    "in normal mode" must {

      "return answers rows with change links" in {
        CompanyVatVariationsId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__common__cya__vat", List("test-vat"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__trustee__vat_number"))))
        ))
      }
    }

    "in update mode" must {

      "return answers rows with no links if vat is available " in {
        CompanyVatVariationsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__common__cya__vat", List("test-vat"), false,
            None)
        ))
      }

      "return answers rows with add link if vat is not available" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyVatVariationsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__common__cya__vat", Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some("messages__visuallyhidden__trustee__vat_number_add"))))
        ))
      }
    }
  }
}
