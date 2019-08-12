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
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

class TrusteeUTRIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test company name"
  val utr = "1234567890"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(messages("messages__common__utr"), List(utr), false, Some(Link("site.change", onwardUrl,
      Some(messages("messages__visuallyhidden__trustee__utr")))))
  )

  private val answerRowsWithoutChangeLink = Seq(
    AnswerRow(messages("messages__common__utr"), List(utr), false, None))

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(TrusteeUTRId(0))(utr).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
        TrusteeUTRId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        TrusteeUTRId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "display the row without change link if utr is present" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeUTRId(0).row(onwardUrl, UpdateMode) mustEqual answerRowsWithoutChangeLink
      }

      "display not entered without change link if utr is not present" in {
        val notEntered = Seq(
          AnswerRow(messages("messages__common__utr"), List("site.not_entered"), answerIsMessageKey = true, None))
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeUTRId(0).row(onwardUrl, UpdateMode) mustEqual notEntered
      }
    }
  }
}
