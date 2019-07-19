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
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyDetailsId, OtherDirectorsId}
import models.person.PersonDetails
import models.requests.DataRequest
import models.{CompanyDetails, Link, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

class DirectorDetailsIdSpec extends SpecBase{
  val userAnswersWithTenDirectors = UserAnswers(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails("TestCompanyName"),
        "director" -> Json.arr(
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "One", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Two", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Three", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Four", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Five", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Six", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Seven", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Eight", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Nine", LocalDate.now())),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("Tim", None, "Ten", LocalDate.now(), isDeleted = true)),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("Tim", None, "Eleven", LocalDate.now(), isDeleted = true)),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("Tim", None, "Twelve", LocalDate.now(), isDeleted = true)),
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Thirteen", LocalDate.now()))
        )
      ))))

  val userAnswersWithOneDirector = UserAnswers(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails("TestCompanyName"),
        "director" -> Json.arr(
          Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "One", LocalDate.now()))
        )
      ))))

  "Cleanup" must {

    "remove MoreThanTenDirectorsId" when {

      "there are fewer than 10 directors" in {

        val result: UserAnswers = userAnswersWithOneDirector
          .set(OtherDirectorsId(0))(true).asOpt.value
          .remove(DirectorDetailsId(0, 0)).asOpt.value

        result.get(OtherDirectorsId(0)) must not be defined

      }

      "there are 10 directors" in {

        val result: UserAnswers = userAnswersWithTenDirectors
          .set(OtherDirectorsId(0))(true).asOpt.value
          .remove(DirectorDetailsId(0, 0)).asOpt.value

        result.get(OtherDirectorsId(0)) must not be defined

      }

    }

  }

  "cya" when {
    val onwardUrl = "onwardUrl"
    val personDetails = PersonDetails("firstName", None, "last", LocalDate.now)
    val answers = UserAnswers(Json.obj())
      .set(DirectorDetailsId(0, 0))(personDetails)
      .asOpt.value
    implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))

    "in update mode for new directors" must {

      "return answers rows with change links" in {
        val answers = UserAnswers(Json.obj()).set(DirectorDetailsId(0, 0))(personDetails).flatMap(
          _.set(IsNewDirectorId(0, 0))(true)
        ).asOpt.value

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))


        val expectedResult = Seq(
          AnswerRow(
            "messages__director__cya__name",
            Seq(s"${personDetails.fullName}"),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__name", personDetails.fullName))))
          ),
          AnswerRow(
            messages("messages__director__cya__dob", personDetails.firstAndLastName),
            Seq(s"${DateHelper.formatDate(personDetails.date)}"),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__dob", personDetails.fullName))))
          )
        )

        DirectorDetailsId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }
    }

    "in update mode for existing directors" must {

      "return answers rows without change links" in {
        DirectorDetailsId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__director__cya__name", Seq(s"${personDetails.fullName}"), false, None),
          AnswerRow(messages("messages__director__cya__dob", personDetails.firstAndLastName), Seq(s"${DateHelper.formatDate(personDetails.date)}"), false, None)
        ))
      }
    }

    "in normal mode " must {

      "return answers rows with change links" in {

        val expectedResults = Seq(
          AnswerRow(
            "messages__director__cya__name",
            Seq(s"${personDetails.fullName}"),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__name", personDetails.fullName))))
          ),
          AnswerRow(
            messages("messages__director__cya__dob", personDetails.firstAndLastName),
            Seq(s"${DateHelper.formatDate(personDetails.date)}"),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__dob", personDetails.fullName))))
          )
        )

        DirectorDetailsId(0, 0).row(onwardUrl, NormalMode) must equal(expectedResults)
      }
    }
  }
}
