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

package identifiers.register.establishers.company

import identifiers.register.establishers.IsEstablisherNewId
import models.AddressYears.UnderAYear
import models.{Link, NormalMode, UpdateMode}
import models.requests.DataRequest
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{Enumerable, UserAnswers}
import viewmodels.AnswerRow

import utils.checkyouranswers.Ops._

class CompanyTradingTimeIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits  {

  "cya" when {

    val onwardUrl = "onwardUrl"

    def answers: UserAnswers = UserAnswers().set(CompanyTradingTimeId(0))(UnderAYear).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyTradingTimeId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(
            "companyTradingTime.checkYourAnswersLabel",
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some("messages__visuallyhidden__establisher__trading_time")))
          )))
      }
    }

    "in update mode for new trustee - company paye" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyTradingTimeId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(
            "companyTradingTime.checkYourAnswersLabel",
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,
              Some("messages__visuallyhidden__establisher__trading_time")))
          )))
      }
    }

    "in update mode for existing establisher- company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        CompanyTradingTimeId(0).row(onwardUrl, UpdateMode) must equal(Nil)
      }
    }
  }

}
