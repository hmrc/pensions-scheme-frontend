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

package identifiers.register.establishers.individual

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

class EstablisherEnterNINOIdSpec extends SpecBase {

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__enterNINO", Message("messages__theIndividual")),
      List("nino"), false, Some(Link("site.change", onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_national_insurance_number",
        Message("messages__theIndividual"))))))
  )

  "cya" when {

    def answers: UserAnswers = UserAnswers().set(EstablisherEnterNINOId(0))(ReferenceValue("nino")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        EstablisherEnterNINOId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher -  nino" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        EstablisherEnterNINOId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - individual nino" must {

      "return answers rows without change links if nino is available and not editable" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        EstablisherEnterNINOId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__enterNINO", Message("messages__theIndividual")),
            List("nino"), false, None)
        ))
      }

      "return answers rows with change links if nino is available and editable" in {
        val answers = UserAnswers().set(EstablisherEnterNINOId(0))(ReferenceValue("nino", true)).asOpt.get
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        EstablisherEnterNINOId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if nino is not available" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(), Some(PsaId("A0000000")))

        EstablisherEnterNINOId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__enterNINO", Message("messages__theIndividual")),
            Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_national_insurance_number",
                Message("messages__theIndividual"))))))))
      }
    }
  }
}
