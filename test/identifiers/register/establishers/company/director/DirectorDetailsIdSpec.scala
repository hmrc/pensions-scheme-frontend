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

import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyDetailsId, OtherDirectorsId}
import models.CompanyDetails
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class DirectorDetailsIdSpec extends WordSpec with MustMatchers with OptionValues {
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
}
