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
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, ReferenceValue, UpdateMode}
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops.toOps
import viewmodels.{AnswerRow, Message}

class TrusteeHasNINOIdSpec extends SpecBase with OptionValues {

  private val onwardUrl = "onwardUrl"
  private val personDetails = PersonName("first", "last")
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = Message("messages__hasNINO", personDetails.fullName).resolve,
      answer = Seq("site.no"),
      answerIsMessageKey = true,
      changeUrl = Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_hasNino", personDetails.fullName).resolve)))
    )
  )

  "Cleanup" when {
    def answers(hasNino: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(TrusteeHasNINOId(0))(hasNino)
      .flatMap(_.set(TrusteeEnterNINOId(0))(ReferenceValue("test-nino", isEditable = true)))
      .flatMap(_.set(TrusteeNoNINOReasonId(0))("reason"))
      .asOpt.value

    "`TrusteeHasNINO` is set to `false`" must {

      val result: UserAnswers = answers().set(TrusteeHasNINOId(0))(false).asOpt.value

      "remove the data for `TrusteeNino`" in {
        result.get(TrusteeEnterNINOId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasNINO` is set to `true`" must {

      val result: UserAnswers = answers(false).set(TrusteeHasNINOId(0))(true).asOpt.value

      "remove the data for `TrusteeNoNinoReason`" in {
        result.get(TrusteeNoNINOReasonId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasNINO` is not present" must {

      val result: UserAnswers = answers().remove(TrusteeHasNINOId(0)).asOpt.value

      "not remove the data for `TrusteeNoNinoReason`" in {
        result.get(TrusteeNoNINOReasonId(0)) mustBe defined
      }
    }
  }

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(TrusteeNameId(0))(personDetails).asOpt.value
        .set(TrusteeHasNINOId(0))(false).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeHasNINOId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {
      val updatedAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", updatedAnswers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeHasNINOId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "Not return answer rows" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeHasNINOId(0).row(onwardUrl, UpdateMode) must equal(Seq.empty[AnswerRow])
      }
    }
  }
}
