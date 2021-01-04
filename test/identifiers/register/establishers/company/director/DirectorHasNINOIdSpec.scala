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

package identifiers.register.establishers.company.director

import base.SpecBase
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, ReferenceValue, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class DirectorHasNINOIdSpec extends SpecBase {

  private val personDetails = PersonName("first", "last")
  private val onwardUrl = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = Message("messages__hasNINO", personDetails.fullName),
      answer = Seq("site.no"),
      answerIsMessageKey = true,
      changeUrl = Some(Link("site.change", onwardUrl,
        Some(Message("messages__visuallyhidden__dynamic_hasNino", personDetails.fullName))))
    )
  )

  "Cleanup" when {

    def answers(hasNino: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(DirectorHasNINOId(0, 0))(hasNino)
      .flatMap(_.set(DirectorEnterNINOId(0, 0))(ReferenceValue("test-nino", isEditable = true)))
      .flatMap(_.set(DirectorNoNINOReasonId(0, 0))("reason"))
      .asOpt.value

    "`DirectorHasNINO` is set to `false`" must {

      val result: UserAnswers = answers().set(DirectorHasNINOId(0, 0))(false).asOpt.value

      "remove the data for `DirectorNino`" in {
        result.get(DirectorEnterNINOId(0, 0)) mustNot be(defined)
      }
    }

    "`DirectorHasNINO` is set to `true`" must {

      val result: UserAnswers = answers(false).set(DirectorHasNINOId(0, 0))(true).asOpt.value

      "remove the data for `DirectorNoNinoReason`" in {
        result.get(DirectorNoNINOReasonId(0, 0)) mustNot be(defined)
      }
    }

    "`DirectorHasNINO` is not present" must {

      val result: UserAnswers = answers().remove(DirectorHasNINOId(0, 0)).asOpt.value

      "not remove the data for `DirectorNoNinoReason`" in {
        result.get(DirectorNoNINOReasonId(0, 0)) mustBe defined
      }
    }
  }

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(DirectorNameId(0, 0))(personDetails).asOpt.value
        .set(DirectorHasNINOId(0, 0))(false).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        DirectorHasNINOId(0, 0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new partner" must {
      val updatedAnswers = answers.set(IsNewDirectorId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", updatedAnswers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        DirectorHasNINOId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing partner" must {

      "Not return answer rows" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        DirectorHasNINOId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq.empty[AnswerRow])
      }
    }
  }
}
