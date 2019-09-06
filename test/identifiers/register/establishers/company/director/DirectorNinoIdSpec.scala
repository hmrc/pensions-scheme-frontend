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
import models.person.PersonDetails
import models.{Link, Nino, NormalMode, UpdateMode}
import models.requests.DataRequest
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class DirectorNinoIdSpec extends SpecBase {

  "cya" when {
    val onwardUrl = "onwardUrl"

    val directorDetails = PersonDetails("John", None, "One", LocalDate.now())

    def answers(nino: Nino): UserAnswers = UserAnswers(Json.obj())
      .set(DirectorDetailsId(0, 0))(directorDetails).asOpt.value
      .set(DirectorNinoId(0, 0))(nino).asOpt.value

    val ninoYes = Nino.Yes("AB100000A")
    val ninoNo = Nino.No("Not sure")

    "in normal mode" must {

      "return answers rows with change links for nino with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(ninoYes), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__nino", directorDetails.firstAndLastName),
            Seq(s"${Nino.Yes}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino_yes_no")))
          ),
          AnswerRow(
            "messages__common__nino",
            Seq(ninoYes.nino),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino")))
          )
        )

        DirectorNinoId(0, 0).row(onwardUrl, NormalMode) must equal(expectedResult)
      }

      "return answers rows with change links for nino with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(ninoNo), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__nino", directorDetails.firstAndLastName),
            Seq(s"${Nino.No}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino_yes_no")))
          ),
          AnswerRow(
            messages("messages__director__cya__nino_reason", directorDetails.firstAndLastName),
            Seq(ninoNo.reason),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino_no")))
          )
        )

        DirectorNinoId(0, 0).row(onwardUrl, NormalMode) must equal(expectedResult)
      }
    }

    "in update mode for new directors" must {

      def answersNew(nino: Nino): UserAnswers = answers(nino).set(IsNewDirectorId(0, 0))(true).asOpt.value

      "return answers rows with change links for nino with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew(ninoYes), PsaId("A0000000"))
        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__nino", directorDetails.firstAndLastName),
            Seq(s"${Nino.Yes}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino_yes_no")))
          ),
          AnswerRow(
            "messages__common__nino",
            Seq(ninoYes.nino),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino")))
          )
        )

        DirectorNinoId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }

      "return answers rows with change links for nino with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew(ninoNo), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__nino", directorDetails.firstAndLastName),
            Seq(s"${Nino.No}"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino_yes_no")))),
          AnswerRow(
            messages("messages__director__cya__nino_reason", directorDetails.firstAndLastName),
            Seq(ninoNo.reason), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__nino_no"))))
        )

        DirectorNinoId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }
    }

    "in update mode for existing directors" must {

      "return answers rows without change links for nino with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(ninoYes), PsaId("A0000000"))

        DirectorNinoId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__common__nino", Seq(ninoYes.nino), false, None)))
      }

      "return answers rows with change links for nino with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(ninoNo), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            "messages__common__nino",
            Seq("site.not_entered"),
            true,
            Some(Link("site.add", onwardUrl, Some("messages__visuallyhidden__director__nino")))
          )
        )

        DirectorNinoId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }
    }
  }
}
