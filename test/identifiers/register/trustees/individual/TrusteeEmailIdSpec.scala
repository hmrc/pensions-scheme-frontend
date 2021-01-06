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

package identifiers.register.trustees.individual

import base.SpecBase
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class TrusteeEmailIdSpec extends SpecBase {

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
    val email = "test@test.com"
    val onwardUrl = "onwardUrl"
    val personName = PersonName("first", "last")
    val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
      UserAnswers().set(TrusteeEmailId(0))(email).flatMap(
        _.set(TrusteeNameId(0))(personName)).asOpt.value, Some(PsaId("A0000000")))


    Seq(NormalMode, UpdateMode).foreach { mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          TrusteeEmailId(0).row(onwardUrl, mode)(request, implicitly) must equal(Seq(
            AnswerRow(
              Message("messages__enterEmail", personName.fullName),
              Seq(email),
              answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_email_address", personName.fullName))))
            )))
        }
      }
    }
  }
}