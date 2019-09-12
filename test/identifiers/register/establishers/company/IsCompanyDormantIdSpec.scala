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

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.register.DeclarationDormant._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{Enumerable, UserAnswers}
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class IsCompanyDormantIdSpec extends SpecBase with Enumerable.Implicits {

  private val companyName = "the company"

  "cya" when {
    
    val onwardUrl = "onwardUrl"

    def answers = UserAnswers().set(IsCompanyDormantId(0))(Yes).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        IsCompanyDormantId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(messages("messages__company__cya__dormant"),List("site.yes"),true,
            Some(Link("site.change",onwardUrl,Some(messages("messages__visuallyhidden__dynamic_company__dormant", companyName)))))
        ))
      }
    }

    "in update mode for new establisher - company" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        IsCompanyDormantId(0).row(onwardUrl, UpdateMode) must equal(Seq())
      }
    }

    "in update mode for existing establisher - company" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        IsCompanyDormantId(0).row(onwardUrl, UpdateMode) must equal(Seq())
      }
    }
  }
}
