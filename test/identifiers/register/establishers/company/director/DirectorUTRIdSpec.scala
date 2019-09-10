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
import viewmodels.AnswerRow

class DirectorUTRIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test company name"
  val utr = "1234567890"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow("messages__utr__checkyouranswerslabel", List(utr), false, Some(Link("site.change",onwardUrl,
      Some("messages__visuallyhidden__companyUTR"))))
  )

  private val answerRowsWithoutChangeLink = Seq(
    AnswerRow("messages__utr__checkyouranswerslabel", List(utr), false, None))

  "Cleanup" when {
    def answers: UserAnswers = UserAnswers(Json.obj())
      .set(DirectorNoUTRReasonId(0, 0))("reason").asOpt.value

    "remove the data for `NoCompanyUTRReason`" in {
      val result: UserAnswers = answers.set(DirectorUTRId(0, 0))(ReferenceValue("utr")).asOpt.value
      result.get(DirectorNoUTRReasonId(0, 0)) mustNot be(defined)
    }
  }

  "cya" when {

    def answers(isEditable: Boolean = false): UserAnswers = UserAnswers().set(DirectorUTRId(0, 0))(ReferenceValue(utr, isEditable)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        DirectorUTRId(0, 0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode" when {
      def answersNew: UserAnswers = answers().set(IsNewDirectorId(0, 0))(true).asOpt.value

      "for new director" must {

        "return answers rows with change links" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers
          DirectorUTRId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
        }
      }

      "for existing director" must {

        "return row with add link if there is no data available" in {
          val answerRowWithAddLink = AnswerRow("messages__utr__checkyouranswerslabel", List("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add",onwardUrl,
              Some("messages__visuallyhidden__companyUTR")
              )))
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers().trusteesCompanyDetails(index = 0, CompanyDetails(name)), PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          DirectorUTRId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Seq(answerRowWithAddLink)
        }

        "return row without change link if there is data avalable and is not editable" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(), PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          DirectorUTRId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual answerRowsWithoutChangeLink
        }

        "return row with change link if there is data available and is editable" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = true), PsaId("A0000000"))
          implicit val userAnswers: UserAnswers = request.userAnswers

          DirectorUTRId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual answerRowsWithChangeLinks
        }
      }
    }
  }
}
