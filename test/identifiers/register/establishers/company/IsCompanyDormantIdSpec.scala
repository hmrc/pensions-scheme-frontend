/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.register.DeclarationDormant._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerRow, Message}

class IsCompanyDormantIdSpec extends SpecBase with Enumerable.Implicits {

  "cya" when {

    val onwardUrl = "onwardUrl"

    def answers: UserAnswers = UserAnswers().set(IsCompanyDormantId(0))(Yes).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        implicit val userAnswers: UserAnswers = request.userAnswers

        IsCompanyDormantId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(
          Seq(AnswerRow(
              label = Message("messages__company__cya__dormant", Message("messages__theCompany")),
              answer = List("site.yes"),
              answerIsMessageKey = true,
              changeUrl = Some(Link("site.change", onwardUrl,
                Some(Message("messages__visuallyhidden__dynamic_company__dormant", Message("messages__theCompany")))))
            ))
        )
      }
    }

    "in update mode " must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(value = true).asOpt.value

      "return no answer rows" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        IsCompanyDormantId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Nil)
      }
    }
  }
}
