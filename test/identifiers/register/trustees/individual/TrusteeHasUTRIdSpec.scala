/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class TrusteeHasUTRIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = PersonName("test", "name")
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = messages("messages__hasUTR", name.fullName),
      answer = List("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_hasUtr", name.fullName))))
    )
  )

  "Cleanup" when {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(TrusteeHasUTRId(0))(hasUtr)
      .flatMap(_.set(TrusteeUTRId(0))(ReferenceValue("test-utr")))
      .flatMap(_.set(TrusteeNoUTRReasonId(0))("reason"))
      .asOpt.value

    "`TrusteeHasUTR` is set to `false`" must {

      val result: UserAnswers = answers().set(TrusteeHasUTRId(0))(false).asOpt.value

      "remove the data for `TrusteeUTR`" in {
        result.get(TrusteeUTRId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasUTR` is set to `true`" must {

      val result: UserAnswers = answers(false).set(TrusteeHasUTRId(0))(true).asOpt.value

      "remove the data for `NoUTRReason`" in {
        result.get(TrusteeNoUTRReasonId(0)) mustNot be(defined)
      }
    }

    "`TrusteeHasUTR` is not present" must {

      val result: UserAnswers = answers().remove(TrusteeHasUTRId(0)).asOpt.value

      "not remove the data for `TrusteeUTR`" in {
        result.get(TrusteeUTRId(0)) mustBe defined
      }

      "not remove the data for `NoUTRReason`" in {
        result.get(TrusteeNoUTRReasonId(0)) mustBe defined
      }
    }
  }

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(TrusteeNameId(0))(name).flatMap(
      _.set(TrusteeHasUTRId(0))(true)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        TrusteeHasUTRId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        TrusteeHasUTRId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "not display any row" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeHasUTRId(0).row(onwardUrl, UpdateMode) mustEqual Nil
      }
    }
  }
}
