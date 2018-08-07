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

import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.{IsTrusteeCompleteId, TrusteesId}
import models.{CompanyDetails, PartnershipDetails}
import models.person.PersonDetails
import models.register._
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind.{Company, Indivdual, Partnership}
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.Individual
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues {

  import UserAnswersSpec._

  ".allEstablishers" must {
    "return a sequence of establishers names, edit links and delete links" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherCompanyDetailsId.toString ->
              CompanyDetails("my company", None, None),
            IsEstablisherCompleteId.toString -> true
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name", LocalDate.now),
            IsEstablisherCompleteId.toString -> false
          ),
          Json.obj(
            PartnershipDetailsId.toString ->
              PartnershipDetails("my partnership name"),
            IsEstablisherCompleteId.toString -> false
          )
        )
      )
      val userAnswers = UserAnswers(json)
      val allEstablisherEntities = Seq(establisherEntity("my company", 0, Company, isComplete = true),
        establisherEntity("my name", 1, Indivdual), establisherEntity("my partnership name", 2, Partnership))

      userAnswers.allEstablishers mustEqual allEstablisherEntities
    }

    "return en empty sequence if there are no establishers" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allEstablishers mustEqual Seq.empty
    }

    "return en empty sequence if the json is invalid" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            "invalid" -> "invalid"
          )
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allEstablishers mustEqual Seq.empty
    }
  }

  ".allEstablishersAfterDelete" must {
    "return a map of establishers names, edit links and delete links when one of the establishers is deleted" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name 1", LocalDate.now)
          ),
          Json.obj(
            EstablisherCompanyDetailsId.toString ->
              CompanyDetails("my company 2", None, None, isDeleted = true)
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name 3", LocalDate.now)
          )
        )
      )

      val userAnswers = UserAnswers(json)
      val allEstablisherEntities = Seq(establisherEntity("my name 1", 0, Indivdual), establisherEntity("my name 3", 2, Indivdual))

      userAnswers.allEstablishersAfterDelete mustEqual allEstablisherEntities
    }
  }

  ".allTrustees" must {

    "return a map of trustee names, edit links, delete links and isComplete flag" in {
      val userAnswers = UserAnswers(Json.obj(
        TrusteesId.toString -> Json.arr(
          Json.obj(
            TrusteeDetailsId.toString ->
              PersonDetails("First", None, "Last", LocalDate.now),
            IsTrusteeCompleteId.toString -> true
          ),
          Json.obj(
            TrusteeCompanyDetailsId.toString ->
              CompanyDetails("My Company", None, None),
            IsTrusteeCompleteId.toString -> false
          )
        )
      ))

      val allTrusteesEntities = Seq(trusteeEntity("First Last", 0, TrusteeKind.Individual, isComplete = true),
        trusteeEntity("My Company", 1, TrusteeKind.Company))

      val result = userAnswers.allTrustees

      result mustEqual allTrusteesEntities
    }

    "return en empty sequence if there are no trustees" in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees mustEqual Seq.empty
    }

    "return en empty sequence if the json is invalid" in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
          Json.obj(
            "invalid" -> "invalid"
          )
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees mustEqual Seq.empty
    }
  }

  ".allTrusteesAfterDelete" must {

    "return a map of trustee names, edit links and delete links when one of the trustee is deleted" in {
      val userAnswers = UserAnswers()
        .set(TrusteeDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true))
        .flatMap(_.set(identifiers.register.trustees.company.CompanyDetailsId(1))(CompanyDetails("My Company", None, None))).get
      val allTrusteesEntities = Seq(trusteeEntity("My Company", 1, TrusteeKind.Company))

      val result = userAnswers.allTrusteesAfterDelete

      result mustEqual allTrusteesEntities
    }
  }

  ".allDirectors" must {

    "return a map of director names, edit links, delete links and isComplete flag" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(PersonDetails("First", None, "Last", LocalDate.now))
        .flatMap(_.set(IsDirectorCompleteId(0, 0))(true))
        .flatMap(_.set(IsDirectorCompleteId(0, 1))(false))
        .flatMap(_.set(DirectorDetailsId(0, 1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

      val directorEntities = Seq(
        DirectorEntity(DirectorDetailsId(0, 0), "First Last", isDeleted = false, isCompleted = true),
        DirectorEntity(DirectorDetailsId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false))

      val result = userAnswers.allDirectors(0)

      result.size mustEqual 2
      result mustBe directorEntities
    }
  }

  ".allDirectorsAfterDelete" must {

    "return a map of director names, edit links and delete links after one of the directors is deleted" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true))
        .flatMap(_.set(DirectorDetailsId(0, 1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

      val directorEntities = Seq(
        DirectorEntity(DirectorDetailsId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false))
      val result = userAnswers.allDirectorsAfterDelete(0)

      result.size mustEqual 1
      result mustBe directorEntities
    }
  }

  ".establishersCount" must {

    "return the count of all establishers irrespective of whether they are deleted or not" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherCompanyDetailsId.toString ->
              CompanyDetails("my company", None, None)
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name", LocalDate.now, isDeleted = true)
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name", LocalDate.now)
          )
        )
      )
      val userAnswers = UserAnswers(json)

      val result = userAnswers.establishersCount
      result mustEqual 3
    }
  }

  ".trusteesCount" must {

    "return the count of all trustees irrespective of whether they are deleted or not" in {
      val userAnswers = UserAnswers()
        .set(TrusteeDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true))
        .flatMap(_.set(identifiers.register.trustees.company.CompanyDetailsId(1))(CompanyDetails("My Company", None, None))).get

      val result = userAnswers.trusteesCount
      result mustEqual 2
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

object UserAnswersSpec {
  private def establisherEntity(name: String, index: Int, establisherKind: EstablisherKind, isComplete: Boolean = false) = {
    establisherKind match {
      case Indivdual =>
        EstablisherIndividualEntity(EstablisherDetailsId(index), name, isDeleted = false, isCompleted = isComplete)
      case Company =>
        EstablisherCompanyEntity(EstablisherCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete)
      case _ =>
        EstablisherPartnershipEntity(PartnershipDetailsId(index), name, isDeleted = false, isCompleted = isComplete)
    }
  }

  private def trusteeEntity(name: String, index: Int, trusteeKind: TrusteeKind, isComplete: Boolean = false) = {
    trusteeKind match {
      case Individual =>
        TrusteeIndividualEntity(TrusteeDetailsId(index), name, isDeleted = false, isCompleted = isComplete)
      case _ =>
        TrusteeCompanyEntity(TrusteeCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete)
    }
  }

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
}
