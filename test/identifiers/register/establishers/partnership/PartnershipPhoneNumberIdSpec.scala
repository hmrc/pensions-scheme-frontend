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

package identifiers.register.establishers.partnership

import base.SpecBase
import models.*
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops.*
import utils.{CountryOptions, InputOption, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnershipPhoneNumberIdSpec extends SpecBase {

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
    val index = 0
    val phone = "0111"
    val onwardUrl = "onwardUrl"
    val partnershipDetails = PartnershipDetails("test partnership")
    Seq(NormalMode, UpdateMode).foreach { mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers().establisherPartnershipDetails(Index(0), partnershipDetails)
              .set(PartnershipPhoneNumberId(index))(phone).asOpt.value, Some(PsaId("A0000000")))

          PartnershipPhoneNumberId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__enterPhoneNumber", partnershipDetails.name),
              Seq(phone),
              answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_phone_number", partnershipDetails.name))))
            )))
        }
      }
    }
  }
}