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

package identifiers.register.trustees.individual

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models.{Link, NormalMode, UpdateMode}
import models.person.PersonName
import models.requests.DataRequest
import org.scalatest.OptionValues
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, InputOption, UserAnswers}
import utils.checkyouranswers.Ops.toOps
import viewmodels.{AnswerRow, Message}

class TrusteeNoUTRReasonIdSpec extends SpecBase with OptionValues {

  private val onwardUrl = "onwardUrl"
  private val personDetails = PersonName("first", "last")
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = messages("messages__whyNoUTR", personDetails.fullName),
      answer = Seq("blah"),
      answerIsMessageKey = false,
      changeUrl = Some(Link("site.change", onwardUrl,
        Some(messages("messages__visuallyhidden__dynamic_noUtrReason", personDetails.fullName))))
    )
  )

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(TrusteeNameId(0))(personDetails).asOpt.value
        .set(TrusteeNoUTRReasonId(0))("blah").asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

        TrusteeNoUTRReasonId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {
      val updatedAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", updatedAnswers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

        TrusteeNoUTRReasonId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "Not return answer rows" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

        TrusteeNoUTRReasonId(0).row(onwardUrl, UpdateMode) must equal(Seq.empty[AnswerRow])
      }
    }
  }
}
