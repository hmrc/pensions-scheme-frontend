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

package identifiers.register.trustees.partnership

import base.SpecBase
import models.*
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops.*
import utils.{CountryOptions, InputOption, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnershipEmailIdSpec extends SpecBase {

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
    val email = "test@test.com"
    val onwardUrl = "onwardUrl"
    val partnershipDetails = PartnershipDetails("test partnership")
    val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
      UserAnswers().trusteePartnershipEmail(Index(0), email).trusteePartnershipDetails(Index(0), partnershipDetails), Some(PsaId("A0000000")))


    Seq(NormalMode, UpdateMode).foreach { mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          PartnershipEmailId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__enterEmail", partnershipDetails.name),
              Seq(email),
              answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_email_address", partnershipDetails.name))))
            )))
        }
      }
    }
  }
}