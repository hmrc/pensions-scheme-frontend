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
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

class CompanyRegistrationNumberVariationsIdSpec extends SpecBase {

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)

  "cya" when {

    val onwardUrl = "onwardUrl"

    def answers: UserAnswers = UserAnswers().set(CompanyRegistrationNumberVariationsId(0))(CompanyRegistrationNumberVariationsId.toString).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyRegistrationNumberVariationsId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__checkYourAnswers__trustees__company__number", List("crn"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__trustee__crn"))))
        ))
      }
    }

    "in update mode for new trustee - company crn" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyRegistrationNumberVariationsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__checkYourAnswers__trustees__company__number", List("crn"), false, None)))
      }
    }

    "in update mode for existing trustee - company crn" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyRegistrationNumberVariationsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__checkYourAnswers__trustees__company__number", List("crn"), false, None)))
      }
    }

    "display an add link if no answer if found" in {
      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(), PsaId("A0000000"))
      implicit val userAnswers = request.userAnswers
      CompanyRegistrationNumberVariationsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
        AnswerRow("messages__checkYourAnswers__trustees__company__number", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link("site.add", onwardUrl, Some("messages__visuallyhidden__trustee__crn_add"))))))
    }
  }
}
