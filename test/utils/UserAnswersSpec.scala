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

package utils

import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId, CompanyPayeId => EstablisherCompanyPayeId, CompanyVatId => EstablisherCompanyVatId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.trustees.company.{CompanyPayeId, CompanyVatId, CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.{company => _, _}
import models._
import models.person.PersonDetails
import models.register.SchemeType.SingleTrust
import models.register._
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind.{Company, Indivdual, Partnership}
import models.register.trustees.TrusteeKind
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import UserAnswersSpec._

  ".allEstablishers" must {
    "return a sequence of establishers names, edit links and delete links" in {
      val userAnswers = UserAnswers().set(
        EstablisherCompanyDetailsId(0))(CompanyDetails("my company")).flatMap(
        _.set(IsEstablisherCompleteId(0))(true).flatMap(
          _.set(EstablisherKindId(0))(EstablisherKind.Company).flatMap(
            _.set(IsEstablisherNewId(0))(true).flatMap(
              _.set(EstablisherCompanyVatId(0))(Vat.No).flatMap(
                _.set(EstablisherCompanyPayeId(0))(Paye.No).flatMap(
        _.set(EstablisherDetailsId(1))(PersonDetails("my", None, "name", LocalDate.now)).flatMap(
          _.set(IsEstablisherNewId(1))(true).flatMap(
            _.set(IsEstablisherCompleteId(1))(false).flatMap(
              _.set(EstablisherKindId(1))(EstablisherKind.Indivdual).flatMap(
        _.set(PartnershipDetailsId(2))(PartnershipDetails("my partnership name", false)).flatMap(
          _.set(IsEstablisherNewId(2))(true).flatMap(
            _.set(EstablisherKindId(2))(EstablisherKind.Partnership).flatMap(
              _.set(IsEstablisherCompleteId(2))(false).flatMap(
        _.set(EstablisherKindId(3))(EstablisherKind.Company)).flatMap(
          _.set(IsEstablisherNewId(0))(true)
                          )))))))))))))).asOpt.value

      val allEstablisherEntities: Seq[Establisher[_]] = Seq(
        establisherEntity("my company", 0, Company, isComplete = true),
        establisherEntity("my name", 1, Indivdual),
        establisherEntity("my partnership name", 2, Partnership),
        EstablisherSkeletonEntity(EstablisherKindId(3))
      )

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
              PersonDetails("my", None, "name 1", LocalDate.now),
            IsEstablisherNewId.toString -> true,
            EstablisherKindId.toString -> EstablisherKind.Indivdual.toString
          ),
          Json.obj(
            EstablisherCompanyDetailsId.toString ->
              CompanyDetails("my company 2", isDeleted = true),
            IsEstablisherNewId.toString -> true,
            EstablisherKindId.toString -> EstablisherKind.Company.toString
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name 3", LocalDate.now),
            IsEstablisherNewId.toString -> true,
            EstablisherKindId.toString -> EstablisherKind.Indivdual.toString
          ),
          Json.obj(
            EstablisherKindId.toString -> EstablisherKind.Company.toString,
            IsEstablisherNewId.toString -> true
          )
        )
      )

      val userAnswers = UserAnswers(json)
      val allEstablisherEntities: Seq[Establisher[_]] = Seq(establisherEntity("my name 1", 0, Indivdual), establisherEntity("my name 3", 2, Indivdual))

      userAnswers.allEstablishersAfterDelete mustEqual allEstablisherEntities
    }
  }

  ".allTrustees" must {

    "return a map of trustee names, edit links, delete links and isComplete flag" in {
      val userAnswers = UserAnswers(Json.obj(
        "schemeType"-> Json.obj("name"-> "single"),
        TrusteesId.toString -> Json.arr(
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Individual.toString,
            TrusteeDetailsId.toString ->
              PersonDetails("First", None, "Last", LocalDate.now),
            IsTrusteeCompleteId.toString -> true,
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Company.toString,
            TrusteeCompanyDetailsId.toString ->
              CompanyDetails("My Company"),
            CompanyVatId.toString -> Vat.No,
            CompanyPayeId.toString -> Paye.No,
            IsTrusteeCompleteId.toString -> true,
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Partnership.toString,
            partnership.PartnershipDetailsId.toString ->
              PartnershipDetails("My Partnership", isDeleted = false),
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Individual.toString,
            IsTrusteeNewId.toString -> true
          )
        )
      ))

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(
        trusteeEntity("First Last", 0, TrusteeKind.Individual, isComplete = true),
        trusteeEntity("My Company", 1, TrusteeKind.Company, isComplete = true),
        trusteeEntity("My Partnership", 2, TrusteeKind.Partnership),
        TrusteeSkeletonEntity(TrusteeKindId(3))
      )

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
      val json = Json.obj(
        "schemeType"-> Json.obj("name"-> "single"),
        TrusteesId.toString -> Json.arr(
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Individual.toString,
            TrusteeDetailsId.toString -> PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true),
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Company.toString,
            identifiers.register.trustees.company.CompanyDetailsId.toString -> CompanyDetails("My Company"),
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Individual.toString,
            TrusteeDetailsId.toString -> PersonDetails("FName", None, "LName", LocalDate.now, isDeleted = true),
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Company.toString,
            IsTrusteeNewId.toString -> true
          )
        )
      )

      val userAnswers = UserAnswers(json)

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(trusteeEntity("My Company", 1, TrusteeKind.Company))

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
        DirectorEntity(DirectorDetailsId(0, 0), "First Last", isDeleted = false, isCompleted = true, isNewEntity = true, 2),
        DirectorEntity(DirectorDetailsId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false, isNewEntity = true, 2))

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
        DirectorEntity(DirectorDetailsId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false, isNewEntity = true, 1))
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
              CompanyDetails("my company")
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
        .flatMap(_.set(identifiers.register.trustees.company.CompanyDetailsId(1))(CompanyDetails("My Company"))).get

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

    "return true if an establisher is a partnership" in {
      val answers =
        UserAnswers()
          .set(EstablisherDetailsId(0))(person)
          .flatMap(_.set(PartnershipDetailsId(1))(partnershipDetails))
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
  private def establisherEntity(name: String, index: Int, establisherKind: EstablisherKind, isComplete: Boolean = false): Establisher[_] = {
    establisherKind match {
      case Indivdual =>
        EstablisherIndividualEntity(EstablisherDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, 4)
      case Company =>
        EstablisherCompanyEntity(EstablisherCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, 4)
      case _ =>
        EstablisherPartnershipEntity(PartnershipDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, 4)
    }
  }

  private def trusteeEntity(name: String, index: Int, trusteeKind: TrusteeKind, isComplete: Boolean = false): Trustee[_] = {
    trusteeKind match {
      case TrusteeKind.Individual =>
        TrusteeIndividualEntity(TrusteeDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, 4, SingleTrust.toString)
      case TrusteeKind.Company =>
        TrusteeCompanyEntity(TrusteeCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, 4, SingleTrust.toString)
      case _ =>
        TrusteePartnershipEntity(partnership.PartnershipDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, 4, SingleTrust.toString)
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

  private val company = CompanyDetails("test-company-name")
  private val person = PersonDetails("test-first-name", None, "test-last-name", LocalDate.now())
  private val partnershipDetails = PartnershipDetails("test-first-name")
}
