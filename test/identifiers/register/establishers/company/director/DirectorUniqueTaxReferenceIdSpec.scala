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
import models.requests.DataRequest
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class DirectorUniqueTaxReferenceIdSpec extends SpecBase {

  "cya" when {
    val onwardUrl = "onwardUrl"

    val directorDetails = PersonDetails("John", None, "One", LocalDate.now())

    def answers(utr: UniqueTaxReference): UserAnswers = UserAnswers(Json.obj())
      .set(DirectorDetailsId(0, 0))(directorDetails).asOpt.value
      .set(DirectorUniqueTaxReferenceId(0, 0))(utr).asOpt.value

    val utrYes = UniqueTaxReference.Yes("1111111111")
    val utrNo = UniqueTaxReference.No("Not sure")

    "in normal mode" must {

      "return answers rows with change links for utr with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrYes), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__utr_yes_no", directorDetails.firstAndLastName),
            Seq(s"${UniqueTaxReference.Yes}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr_yes_no")))
          ),
          AnswerRow(
            "messages__establisher_individual_utr_cya_label",
            Seq(utrYes.utr),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr")))
          )
        )

        DirectorUniqueTaxReferenceId(0, 0).row(onwardUrl, NormalMode) must equal(expectedResult)
      }

      "return answers rows with change links for utr with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrNo), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__utr_yes_no", directorDetails.firstAndLastName),
            Seq(s"${UniqueTaxReference.No}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr_yes_no")))
          ),
          AnswerRow(
            messages("messages__director__cya__utr_no_reason", directorDetails.firstAndLastName),
            Seq(utrNo.reason),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr_no")))
          )
        )

        DirectorUniqueTaxReferenceId(0, 0).row(onwardUrl, NormalMode) must equal(expectedResult)
      }
    }

    "in update mode for new directors" must {

      def answersNew(utr: UniqueTaxReference): UserAnswers = answers(utr).set(IsNewDirectorId(0, 0))(true).asOpt.value

      "return answers rows with change links for utr with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew(utrYes), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__utr_yes_no", directorDetails.firstAndLastName),
            Seq(s"${UniqueTaxReference.Yes}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr_yes_no")))
          ),
          AnswerRow(
            "messages__establisher_individual_utr_cya_label",
            Seq(utrYes.utr),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr")))
          )
        )
        DirectorUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }

      "return answers rows with change links for utr with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew(utrNo), PsaId("A0000000"))

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__utr_yes_no", directorDetails.firstAndLastName),
            Seq(s"${UniqueTaxReference.No}"),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr_yes_no")))
          ),
          AnswerRow(
            messages("messages__director__cya__utr_no_reason", directorDetails.firstAndLastName),
            Seq(utrNo.reason),
            false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__utr_no")))
          )
        )

        DirectorUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }
    }

    "in update mode for existing directors" must {

      "return answers rows without change links for utr with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrYes), PsaId("A0000000"))

        DirectorUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__establisher_individual_utr_cya_label", Seq(utrYes.utr), false, None)
        ))
      }

      "return answers rows with change links for utr with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrNo), PsaId("A0000000"))

        DirectorUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__establisher_individual_utr_cya_label", Seq("site.not_entered"), true, None)
        ))
      }
    }
  }
}
