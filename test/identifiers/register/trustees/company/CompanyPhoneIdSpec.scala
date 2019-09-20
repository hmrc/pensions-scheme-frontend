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
import models.requests.DataRequest
import models.{CompanyDetails, Link, NormalMode, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class CompanyPhoneIdSpec extends SpecBase {

  "cya" when {
    val phone = "0111"
    val onwardUrl = "onwardUrl"
    val companyDetails = CompanyDetails("test company")
    val request: DataRequest[AnyContent] = DataRequest(
      FakeRequest(),
      "id",
      UserAnswers().trusteesCompanyDetails(0, companyDetails).trusteeCompanyPhone(0, phone), PsaId("A0000000")
    )
    implicit val userAnswers: UserAnswers = request.userAnswers
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

    Seq(NormalMode, UpdateMode).foreach { mode =>
      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          CompanyPhoneId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              label = Message("messages__common_phone__heading", companyDetails.companyName),
              answer = Seq(phone),
              answerIsMessageKey = false,
              changeUrl = Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_phone", companyDetails.companyName))))
            )))
        }
      }
    }
  }
}
