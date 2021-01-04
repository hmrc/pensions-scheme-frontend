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

package identifiers.register.trustees.company

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class CompanyNoUTRReasonIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test company name"
  val reason = "some lame reason"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__whyNoUTR", name), List(reason), false, Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_noUtrReason", name)))))
  )

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(name)).flatMap(
      _.set(CompanyNoUTRReasonId(0))(reason)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyNoUTRReasonId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - company paye" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyNoUTRReasonId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company paye" must {

      "not display any row" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyNoUTRReasonId(0).row(onwardUrl, UpdateMode) mustEqual Nil
      }
    }
  }
}
