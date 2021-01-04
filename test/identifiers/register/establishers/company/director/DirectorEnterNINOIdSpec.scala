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
import models._
import models.person.PersonName
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

class DirectorEnterNINOIdSpec extends SpecBase {

  private val userAnswersWithName: UserAnswers =
    UserAnswers()
      .set(DirectorNameId(0, 0))(PersonName("first", "last"))
      .asOpt
      .value

  private val name                            = "first last"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl                       = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      Message("messages__common__nino", name),
      List("nino"),
      false,
      Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_nino", name))))
    )
  )

  "Cleanup" when {
    def answers: UserAnswers =
      UserAnswers(Json.obj())
        .set(DirectorNoNINOReasonId(0, 0))("reason")
        .asOpt
        .value

    "remove the data for `DirectorNoNINOReason`" in {
      val result: UserAnswers = answers.set(DirectorEnterNINOId(0, 0))(ReferenceValue("nino", true)).asOpt.value
      result.get(DirectorNoNINOReasonId(0, 0)) mustNot be(defined)
    }
  }

  "cya" when {

    def answers: UserAnswers = userAnswersWithName.set(DirectorEnterNINOId(0, 0))(ReferenceValue("nino")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        DirectorEnterNINOId(0, 0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new director -  nino" must {

      def answersNew: UserAnswers = answers.set(IsNewDirectorId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        DirectorEnterNINOId(0, 0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company director nino" must {

      val answers: UserAnswers =
        userAnswersWithName
          .set(DirectorEnterNINOId(0, 0))(ReferenceValue("nino"))
          .asOpt
          .get

      "return answers rows without change links if nino is available and not editable" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers         = request.userAnswers

        DirectorEnterNINOId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__nino", name),List("nino"),false, None)
        ))
      }

      "return answers rows with change links if nino is available and editable" in {
        val answers = userAnswersWithName.set(DirectorEnterNINOId(0, 0))(ReferenceValue("nino", isEditable = true)).asOpt.get
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers         = request.userAnswers

        DirectorEnterNINOId(0, 0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if nino is not available" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", userAnswersWithName, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        DirectorEnterNINOId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__nino", name), Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_nino", name)))))))

      }
    }
  }
}
