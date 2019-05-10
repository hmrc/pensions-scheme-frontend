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
import models.Nino._
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import viewmodels.AnswerRow
import utils.checkyouranswers.Ops._


class TrusteeNinoIdSpec extends SpecBase {

  "cya" when {
    
    val onwardUrl = "onwardUrl"

    def answers = UserAnswers().set(TrusteeNinoId(0))(Yes("nino")).asOpt.get

    "in normal mode individual nino" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        TrusteeNinoId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__trusteeNino_question_cya_label",List("Yes"),false,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__trustee__nino_yes_no")))),
          AnswerRow("messages__common__nino",List("nino"),false,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__trustee__nino"))))
        ))
      }
    }

    "in update mode for new trustee - individual nino" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        TrusteeNinoId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__trusteeNino_question_cya_label",List("Yes"),false,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__trustee__nino_yes_no")))),
          AnswerRow("messages__common__nino",List("nino"),false,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__trustee__nino"))))
        ))
      }
    }

    "in update mode for existing trustee - individual nino" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        TrusteeNinoId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__common__nino",List("nino"),false, None)
        ))
      }
    }
  }
}
