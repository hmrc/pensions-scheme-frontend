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

package identifiers.register.establishers.partnership.partner

import base.SpecBase
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow
import utils.checkyouranswers.Ops._

class PartnerNoUTRReasonIdSpec extends SpecBase {
  private val onwardUrl = "onwardUrl"
  private val name = PersonName("Test", "Name")
  private val reason = "reason"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = messages("messages__whyNoUTR", name.fullName),
      answer = List(reason),
      answerIsMessageKey = false,
      changeUrl = Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_noUtrReason", name.fullName))))
    )
  )

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(PartnerNameId(0, 0))(name).flatMap(
      _.set(PartnerNoUTRReasonId(0, 0))(reason)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))

        PartnerNoUTRReasonId(0, 0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher" must {

      def answersNew: UserAnswers = answers.set(IsNewPartnerId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))

        PartnerNoUTRReasonId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher" must {

      "not display any row" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))


        PartnerNoUTRReasonId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Nil
      }
    }
  }
}
