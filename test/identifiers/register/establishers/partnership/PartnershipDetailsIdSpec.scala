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

package identifiers.register.establishers.partnership

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class PartnershipDetailsIdSpec extends SpecBase {

  "cya" when {
    
    val onwardUrl = "onwardUrl"

    def answers = UserAnswers().establisherPartnershipDetails(0, PartnershipDetails("test-partnership-name"))

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipDetailsId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__name"), List("test-partnership-name"),false,
            Some(Link("site.change",onwardUrl,Some(Message("messages__visuallyhidden__common__name", "test-partnership-name")))))
        ))
      }
    }

    "in update mode for new establisher - partnership details" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

        PartnershipDetailsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__name"), List("test-partnership-name"),false,
            Some(Link("site.change",onwardUrl,Some(Message("messages__visuallyhidden__common__name", "test-partnership-name")))))
        ))
      }
    }

    "in update mode for existing establisher - partnership details" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        PartnershipDetailsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__name"), List("test-partnership-name"),false,None)
        ))
      }
    }
  }
}
