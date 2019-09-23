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

package identifiers.register.establishers.individual

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models.{Link, NormalMode, ReferenceValue, UpdateMode}
import models.person.PersonName
import models.requests.DataRequest
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class EstablisherHasNINOIdSpec extends SpecBase with OptionValues {

  private val onwardUrl = "onwardUrl"
  private val personDetails = PersonName("first", "last")
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = Message("messages__genericHasNino__h1", personDetails.fullName).resolve,
      answer = Seq("site.no"),
      answerIsMessageKey = true,
      changeUrl = Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_hasNino", personDetails.fullName).resolve)))
    )
  )

  "Cleanup" when {
    def answers(hasNino: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(EstablisherHasNINOId(0))(hasNino)
      .flatMap(_.set(EstablisherNewNinoId(0))(ReferenceValue("test-nino", isEditable = true)))
      .flatMap(_.set(EstablisherNoNINOReasonId(0))("reason"))
      .asOpt.value

    "`EstablisherHasNINO` is set to `false`" must {

      val result: UserAnswers = answers().set(EstablisherHasNINOId(0))(false).asOpt.value

      "remove the data for `EstablisherNino`" in {
        result.get(EstablisherNewNinoId(0)) mustNot be(defined)
      }
    }

    "`EstablisherHasNINO` is set to `true`" must {

      val result: UserAnswers = answers(false).set(EstablisherHasNINOId(0))(true).asOpt.value

      "remove the data for `EstablisherNoNinoReason`" in {
        result.get(EstablisherNoNINOReasonId(0)) mustNot be(defined)
      }
    }

    "`EstablisherHasNINO` is not present" must {

      val result: UserAnswers = answers().remove(EstablisherHasNINOId(0)).asOpt.value

      "not remove the data for `EstablisherNoNinoReason`" in {
        result.get(EstablisherNoNINOReasonId(0)) mustBe defined
      }
    }
  }

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(EstablisherNameId(0))(personDetails).asOpt.value
        .set(EstablisherHasNINOId(0))(false).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        EstablisherHasNINOId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {
      val updatedAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", updatedAnswers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        EstablisherHasNINOId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "Not return answer rows" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        EstablisherHasNINOId(0).row(onwardUrl, UpdateMode) must equal(Seq.empty[AnswerRow])
      }
    }
  }
}
