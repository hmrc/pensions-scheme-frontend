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

package identifiers.register.trustees.partnership

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models.Paye._
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class PartnershipPayeIdSpec extends SpecBase {

  "cya" when {
    
    val onwardUrl = "onwardUrl"

    def answers = UserAnswers().set(PartnershipPayeId(0))(Yes("paye")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        PartnershipPayeId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(Seq(
          AnswerRow("messages__partnership__checkYourAnswers__paye",List("site.yes"),true,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__partnership__paye_yes_no")))),
          AnswerRow("messages__common__cya__paye",List("paye"),false,Some(Link("site.change",onwardUrl,
            Some("messages__visuallyhidden__partnership__paye_number"))))
        ))
      }
    }

    "in update mode for new trustee - partnership paye" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        PartnershipPayeId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(Seq(
          AnswerRow("messages__partnership__checkYourAnswers__paye",List("site.yes"),true,
            Some(Link("site.change",onwardUrl,Some("messages__visuallyhidden__partnership__paye_yes_no")))),
          AnswerRow("messages__common__cya__paye",List("paye"),false,Some(Link("site.change",onwardUrl,
            Some("messages__visuallyhidden__partnership__paye_number"))))
        ))
      }
    }

    "in update mode for existing trustee - partnership paye" must {

      "return answers rows without change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        PartnershipPayeId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow("messages__common__cya__paye",List("paye"),false,None)
        ))
      }
    }
  }
}
