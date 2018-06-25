/*
 * Copyright 2018 HM Revenue & Customs
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

package utils

import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.register.establishers.company.director.DirectorDetails
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues {

  private val establishers = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        "name" -> "foo"
      ),
      Json.obj(
        "name" -> "bar"
      )
    )
  )

  private val company = CompanyDetails("test-company-name", None, None)
  private val person = PersonDetails("test-first-name", None, "test-last-name", LocalDate.now())

  ".allEstablishers" must {
    "return a map of establishers names and edit links" in {

      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherCompanyDetailsId.toString ->
              CompanyDetails("my company", None, None)
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name", LocalDate.now)
          )
        )
      )

      val userAnswers = UserAnswers(json)

      userAnswers.allEstablishers mustEqual Seq(
        "my company" ->
          controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0).url,
        "my name" ->
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 1).url
      )
    }
  }

  ".allTrustees" must {

    "return the expected results" in {

      val userAnswers = UserAnswers()
        .set(TrusteeDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now))
        .flatMap(_.set(identifiers.register.trustees.company.CompanyDetailsId(1))(CompanyDetails("My Company", None, None))).get

      val result = userAnswers.allTrustees

      result must contain("First Last" -> controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(
        NormalMode, 0).url)
      result must contain("My Company" -> controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(
        NormalMode, 1).url)
    }
  }

  ".allDirectors" must {

    "return the expected results" in {

      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(DirectorDetails("First", None, "Last", LocalDate.now, false))
        .flatMap(_.set(DirectorDetailsId(0, 1))(DirectorDetails("First1", None, "Last1", LocalDate.now, true))).get

      val result = userAnswers.allDirectors(0)

      result.size mustEqual 1
      result mustBe Seq(DirectorDetails("First", None, "Last", LocalDate.now, false))
    }
  }

  ".getAllRecursive" must {

    "get all matching recursive results" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.getAllRecursive[String](JsPath \ "establishers" \\ "name").value
      values must contain("foo")
      values must contain("bar")
    }

    "return an empty list when there is a relevant structure with no entries" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAllRecursive[String](JsPath \ "establishers" \\ "address").value mustBe empty
    }

    "return `None` when the data at the path doesn't conform to the type we want" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.getAllRecursive[Int](JsPath \ "establishers" \\ "name")
      values mustNot be(defined)
    }
  }

  ".getAll" must {

    "return a list of all matching data" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.getAll[JsValue](JsPath \ "establishers").value
      values must contain(Json.obj("name" -> "foo"))
    }

    "return an empty list when there is a relevant structure with no entries" in {
      val userAnswers = UserAnswers(Json.obj(
        "establishers" -> Json.arr()
      ))
      val values = userAnswers.getAll[JsValue](JsPath \ "establishers").value
      values mustBe empty
    }

    "return `None` when no data exists at the relevant path" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[JsValue](JsPath \ "trustees") mustNot be(defined)
    }

    "return `None` when the data at the path doesn't conform to the type we want" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[String](JsPath \ "establishers") mustNot be(defined)
    }
  }

  ".get" must {

    "get a matching result" in {
      val userAnswers = UserAnswers(establishers)
      val values = userAnswers.get[String](JsPath \ "establishers" \ 0 \ "name").value
      values mustBe "foo"
    }

    "return empty when no matches" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.get[String](JsPath \ "establishers" \ 8) mustNot be(defined)
    }
  }

  ".hasCompanies" must {
    "return true if an establisher is a company" in {
      val answers =
        UserAnswers()
          .set(EstablisherDetailsId(0))(person)
          .flatMap(_.set(EstablisherCompanyDetailsId(1))(company))
          .asOpt
          .value

      answers.hasCompanies mustBe true
    }

    "return true if a trustee is a company" in {
      val answers =
        UserAnswers()
          .set(TrusteeDetailsId(0))(person)
          .flatMap(_.set(TrusteeCompanyDetailsId(1))(company))
          .asOpt
          .value

      answers.hasCompanies mustBe true
    }

    "return true if both an establisher and a trustee are companies" in {
      val answers =
        UserAnswers()
          .set(EstablisherDetailsId(0))(person)
          .flatMap(_.set(EstablisherCompanyDetailsId(1))(company))
          .flatMap(_.set(TrusteeDetailsId(0))(person))
          .flatMap(_.set(TrusteeCompanyDetailsId(1))(company))
          .asOpt
          .value

      answers.hasCompanies mustBe true
    }

    "return false if no establishers or trustees are companies" in {
      val answers =
        UserAnswers()
          .set(EstablisherDetailsId(0))(person)
          .flatMap(_.set(TrusteeDetailsId(0))(person))
          .asOpt
          .value

      answers.hasCompanies mustBe false
    }

    "return false if there are no establishers or trustees" in {
      val answers =
        UserAnswers()

      answers.hasCompanies mustBe false
    }
  }

}
