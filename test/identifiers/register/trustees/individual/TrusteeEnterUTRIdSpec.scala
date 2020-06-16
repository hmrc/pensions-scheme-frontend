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

class TrusteeEnterUTRIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test name"
  val utr = "1234567890"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__enterUTR", name), List(utr), false, Some(Link("site.change", onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", name)))))
  )

  private val answerRowsWithoutChangeLink = Seq(
    AnswerRow(Message("messages__enterUTR", name), List(utr), false, None))


  "Cleanup" when {
    def answers: UserAnswers = UserAnswers(Json.obj())
      .set(TrusteeNoUTRReasonId(0))("reason").asOpt.value

    "remove the data for `TrusteeNoUTRReason`" in {
      val result: UserAnswers = answers.set(TrusteeUTRId(0))(ReferenceValue("utr")).asOpt.value
      result.get(TrusteeNoUTRReasonId(0)) mustNot be(defined)
    }
  }

  "cya" when {

    def answers(isEditable: Boolean = false): UserAnswers = UserAnswers()
      .set(TrusteeNameId(0))(PersonName("test", "name")).asOpt.value
      .set(TrusteeUTRId(0))(ReferenceValue(utr, isEditable)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        TrusteeUTRId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode" when {
      def answersNew: UserAnswers = answers().set(IsTrusteeNewId(0))(value = true).asOpt.value

      "for new trustee" must {

        "return answers rows with change links" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers
          TrusteeUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
        }
      }

      "for existing trustee" must {

        "return row with add link if there is no data available" in {
          val answerRowWithAddLink = AnswerRow(Message("messages__enterUTR", name), List("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add",onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", name))
            )))
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers()
              .set(TrusteeNameId(0))(PersonName("test", "name")).asOpt.value
              .trusteesCompanyDetails(index = 0, CompanyDetails(name)), PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          TrusteeUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Seq(answerRowWithAddLink)
        }

        "return row without change link if there is data available and is not editable" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(), PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          TrusteeUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual answerRowsWithoutChangeLink
        }

        "return row with change link if there is data available and is editable" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = true), PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          TrusteeUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual answerRowsWithChangeLinks
        }
      }
    }
  }
}
