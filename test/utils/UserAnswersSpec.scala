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

import base.JsonFileReader
import helpers.DataCompletionHelper
import identifiers.register.establishers.company.director._
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.{EstablisherDetailsId, EstablisherNameId}
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.partnership.partner.{IsNewPartnerId, IsPartnerCompleteId, PartnerDetailsId}
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.trustees.company.{CompanyPayeId, CompanyVatId, CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.{TrusteeDetailsId, TrusteeNameId}
import identifiers.register.trustees.{company => _, _}
import models._
import models.address.Address
import models.person._
import models.register.SchemeType.SingleTrust
import models.register._
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind.{Company, Indivdual, Partnership}
import models.register.trustees.TrusteeKind
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits with DataCompletionHelper with JsonFileReader {

  import UserAnswersSpec._

  ".allEstablishers" must {
    "return a sequence of establishers names, edit links and delete links" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payloadHnS.json"))

      val allEstablisherEntities: Seq[Establisher[_]] = Seq(
        establisherEntity("Test Company", 0, Company, isComplete = true),
        establisherEntity("Test Individual", 1, Indivdual, isComplete = true),
        establisherEntity("Test Partnership", 2, Partnership, isComplete = true)
      )

      userAnswers.allEstablishers(isHnSEnabled = true, mode) mustEqual allEstablisherEntities
    }

    "return en empty sequence if there are no establishers" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allEstablishers(isHnSEnabled, mode) mustEqual Seq.empty
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
      userAnswers.allEstablishers(isHnSEnabled, mode) mustEqual Seq.empty
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
      val allEstablisherEntities: Seq[Establisher[_]] =
        Seq(establisherEntity("my name 1", 0, Indivdual, countAfterDeleted = 3),
          establisherEntity("my name 3", 2, Indivdual, countAfterDeleted = 3))

      userAnswers.allEstablishersAfterDelete(isHnSEnabled, mode) mustEqual allEstablisherEntities
    }
  }

  ".allTrustees" must {

    "return a map of trustee names, edit links, delete links and isComplete flag and hub and spoke toggle OFF" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payload.json"))

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(
        trusteeEntity("test company", 0, TrusteeKind.Company, isComplete = true),
        trusteeEntity("firstName lastName", 1, TrusteeKind.Individual, isComplete = true),
        trusteeEntity("test partnership", 2, TrusteeKind.Partnership, isComplete = true)
      )

      val result = userAnswers.allTrustees(false)

      result mustEqual allTrusteesEntities
    }


    "return a map of trustee names, edit links, delete links and isComplete flag and hub and spoke toggle ON" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payloadHnS.json"))

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(
        trusteeEntity("test company", 0, TrusteeKind.Company, isComplete = true),
        trusteeEntity("firstName lastName", 1, TrusteeKind.Individual, isComplete = true, isHnsEnabled = true),
        trusteeEntity("test partnership", 2, TrusteeKind.Partnership, isComplete = true)
      )

      val result = userAnswers.allTrustees(isHnSEnabled)

      result mustEqual allTrusteesEntities
    }


    "return en empty sequence if there are no trustees" in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees(false) mustEqual Seq.empty
    }

    "return en empty sequence if there are no trustees when toggle is on" in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees(true) mustEqual Seq.empty
    }

    "return en empty sequence if the json is invalid when toggle is off" in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
          Json.obj(
            "invalid" -> "invalid"
          )
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees(false) mustEqual Seq.empty
    }

    "return en empty sequence if the json is invalid when toggle is on" in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
          Json.obj(
            "invalid" -> "invalid"
          )
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees(true) mustEqual Seq.empty
    }
  }

  ".allTrusteesAfterDelete" must {

    "return a map of trustee names, edit links and delete links when one of the trustee is deleted when toggle is off" in {
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

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(trusteeEntity("My Company", 1, TrusteeKind.Company, countAfterDeleted = 2))

      val result = userAnswers.allTrusteesAfterDelete(false)

      result mustEqual allTrusteesEntities
    }

    "return a map of trustee names, edit links and delete links when one of the trustee is deleted when toggle is on" in {
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

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(trusteeEntity("My Company", 1, TrusteeKind.Company, countAfterDeleted = 2))

      val result = userAnswers.allTrusteesAfterDelete(true)

      result mustEqual allTrusteesEntities
    }
  }

  ".allDirectors" must {

    "return a map of director names, edit links, delete links and" +
      "isComplete flag including deleted items where names are all the same when HnS toggle is off" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(PersonDetails("First", None, "Last", LocalDate.now)).asOpt.value
        .set(DirectorNinoId(0, 0))(Nino.No("reason")).asOpt.value
        .set(DirectorUniqueTaxReferenceId(0, 0))(UniqueTaxReference.Yes("12345678")).asOpt.value
        .set(DirectorAddressId(0, 0))(address).asOpt.value
        .set(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear).asOpt.value
        .set(DirectorContactDetailsId(0, 0))(ContactDetails("a@a.a", "123")).asOpt.value
        .set(DirectorDetailsId(0, 1))(PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true)).asOpt.value
        .set(DirectorDetailsId(0, 2))(PersonDetails("First", None, "Last", LocalDate.now)).asOpt.value

      val directorEntities = Seq(
        DirectorEntityNonHnS(DirectorDetailsId(0, 0), "First Last", isDeleted = false, isCompleted = true, isNewEntity = false, 2),
        DirectorEntityNonHnS(DirectorDetailsId(0, 1), "First Last", isDeleted = true, isCompleted = false, isNewEntity = false, 2),
        DirectorEntityNonHnS(DirectorDetailsId(0, 2), "First Last", isDeleted = false, isCompleted = false, isNewEntity = false, 2))

      val result = userAnswers.allDirectors(0, false)

      result.size mustEqual 3
      result mustBe directorEntities
    }
  }

  ".allDirectors" must {

    "return a map of director names, edit links, delete links and isComplete flag including deleted items where names are all the same when HnS toggle is on" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payloadHnS.json"))
        .set(DirectorNameId(0, 1))(PersonName("First", "Last", isDeleted = true)).asOpt.value
        .set(DirectorNameId(0, 2))(PersonName("First", "Last")).asOpt.value

      val directorEntities = Seq(
        DirectorEntity(DirectorNameId(0, 0), "Director One", isDeleted = false, isCompleted = true, isNewEntity = true, 2),
        DirectorEntity(DirectorNameId(0, 1), "First Last", isDeleted = true, isCompleted = false, isNewEntity = false, 2),
        DirectorEntity(DirectorNameId(0, 2), "First Last", isDeleted = false, isCompleted = false, isNewEntity = false, 2))

      val result = userAnswers.allDirectors(0, isHnSEnabled)

      result.size mustEqual 3
      result mustBe directorEntities
    }
  }

  ".allDirectorsAfterDelete" must {

    "return a map of director names, edit links and delete links after one of the directors is deleted when HnS toggle is off" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true))
        .flatMap(_.set(DirectorDetailsId(0, 1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

      val directorEntities = Seq(
        DirectorEntityNonHnS(DirectorDetailsId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false, isNewEntity = false, 1))
      val result = userAnswers.allDirectorsAfterDelete(0, false)

      result.size mustEqual 1
      result mustBe directorEntities
    }

    "return a map of director names, edit links and delete links after one of the directors is deleted when HnS toggle is on" in {
      val userAnswers = UserAnswers()
        .set(DirectorNameId(0, 0))(PersonName("First", "Last", isDeleted = true))
        .flatMap(_.set(DirectorNameId(0, 1))(PersonName("First1", "Last1"))).get

      val directorEntities = Seq(
        DirectorEntity(DirectorNameId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false, isNewEntity = false, 1))
      val result = userAnswers.allDirectorsAfterDelete(0, true)

      result.size mustEqual 1
      result mustBe directorEntities
    }
  }

  ".allPartners" must {

    "return a map of partner names, edit links, delete links and isComplete flag including deleted items where names are all the same" in {
      val userAnswers = UserAnswers()
        .set(PartnerDetailsId(0, 0))(PersonDetails("First", None, "Last", LocalDate.now))
        .flatMap(_.set(IsPartnerCompleteId(0, 0))(true))
        .flatMap(_.set(IsPartnerCompleteId(0, 1))(false))
        .flatMap(_.set(PartnerDetailsId(0, 1))(PersonDetails("First", None, "Last", LocalDate.now, isDeleted = true)))
        .flatMap(_.set(PartnerDetailsId(0, 2))(PersonDetails("First", None, "Last", LocalDate.now)))
        .flatMap(_.set(IsNewPartnerId(0, 0))(true))
        .get

      val partnerEntities = Seq(
        PartnerEntity(PartnerDetailsId(0, 0), "First Last", isDeleted = false, isCompleted = true, isNewEntity = true, 2),
        PartnerEntity(PartnerDetailsId(0, 1), "First Last", isDeleted = true, isCompleted = false, isNewEntity = false, 2),
        PartnerEntity(PartnerDetailsId(0, 2), "First Last", isDeleted = false, isCompleted = false, isNewEntity = false, 2))

      val result = userAnswers.allPartners(0)

      result.size mustEqual 3
      result mustBe partnerEntities
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

      answers.hasCompanies(isHnSEnabled, mode) mustBe true
    }

    "return true if an establisher is a partnership" in {
      val answers =
        UserAnswers()
          .set(EstablisherDetailsId(0))(person)
          .flatMap(_.set(PartnershipDetailsId(1))(partnershipDetails))
          .asOpt
          .value

      answers.hasCompanies(isHnSEnabled, mode) mustBe true
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

      answers.hasCompanies(isHnSEnabled, mode) mustBe true
    }

    "return false if no establishers or trustees are companies" in {
      val answers =
        UserAnswers()
          .set(EstablisherDetailsId(0))(person)
          .flatMap(_.set(TrusteeDetailsId(0))(person))
          .asOpt
          .value

      answers.hasCompanies(isHnSEnabled, mode) mustBe false
    }

    "return false if there are no establishers or trustees" in {
      val answers =
        UserAnswers()

      answers.hasCompanies(isHnSEnabled, mode) mustBe false
    }
  }

  "areVariationChangesCompleted" when {
    "checking insurance company" must {
       "return false if scheme have insurance and details are missing" in {
         val insuranceCompanyDetails = UserAnswers().investmentRegulated(true)
         insuranceCompanyDetails.areVariationChangesCompleted(false) mustBe false
       }

       "return false if scheme have insurance is not defined" in {
         val insuranceCompanyDetails = UserAnswers()
         insuranceCompanyDetails.areVariationChangesCompleted(false) mustBe false
       }

       "return true if scheme does not have insurance" in {
         val insuranceCompanyDetails = UserAnswers().benefitsSecuredByInsurance(false)
         insuranceCompanyDetails.areVariationChangesCompleted(false) mustBe true
       }

       "return true if scheme have insurance and all the details are present" in {
         insuranceCompanyDetails.areVariationChangesCompleted(false) mustBe true
       }
     }

    "checking trustees" must {
      "return true if trustees are not defined" in {
        insuranceCompanyDetails.areVariationChangesCompleted(false) mustBe true
      }

      "return false if trustees are not completed" in {
        trustee.areVariationChangesCompleted(false) mustBe false
      }

      "return true if trustees are completed" in {
        val trusteeCompleted = trustee.trusteesCompanyContactDetails(0, ContactDetails("z@z.z", "12345"))
        trusteeCompleted.areVariationChangesCompleted(false) mustBe true
      }
    }

    "checking establishers" must {
      "return false if establishers are not completed" in {
        userAnswersHnS.areVariationChangesCompleted(false) mustBe false
      }

      "return false if establishers are completed but directors are not completed" in {
        val establisherCompleted = userAnswersHnS.set(IsEstablisherCompleteId(0))(true).asOpt.get
        establisherCompleted.areVariationChangesCompleted(false) mustBe false
      }

      "return true if establishers company is completed for h&s toggle on" in {
        val establisherCompleted = userAnswersHnS

        establisherCompleted.areVariationChangesCompleted(isHnSEnabled = true) mustBe true
      }

      "return true if establishers partnership is completed" in {
        val establisherCompleted = establisherPartnership
          .trusteesCompanyContactDetails(0, ContactDetails("z@z.z", "12345"))
          .set(IsEstablisherCompleteId(0))(true).flatMap(
          _.set(IsPartnerCompleteId(0,0))(true)).asOpt.get
        establisherCompleted.areVariationChangesCompleted(isHnSEnabled = false) mustBe true
      }
    }
  }

}

object UserAnswersSpec extends OptionValues with Enumerable.Implicits with JsonFileReader {
  private def establisherEntity(name: String, index: Int, establisherKind: EstablisherKind, isComplete: Boolean = false, countAfterDeleted : Int = 3): Establisher[_] = {
    establisherKind match {
      case Indivdual =>
        EstablisherIndividualEntity(EstablisherNameId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted)
      case Company =>
        EstablisherCompanyEntity(EstablisherCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted)
      case _ =>
        EstablisherPartnershipEntity(PartnershipDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted)
    }
  }

  private def trusteeEntity(name: String, index: Int, trusteeKind: TrusteeKind, isComplete: Boolean = false, countAfterDeleted : Int = 3, isHnsEnabled: Boolean = false): Trustee[_] = {
    trusteeKind match {
      case TrusteeKind.Individual if isHnsEnabled =>
        TrusteeIndividualEntity(TrusteeNameId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted, Some(SingleTrust.toString))
      case TrusteeKind.Individual =>
        TrusteeIndividualEntityNonHns(TrusteeDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted, Some(SingleTrust.toString))
      case TrusteeKind.Company =>
        TrusteeCompanyEntity(TrusteeCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted, Some(SingleTrust.toString))
      case _ =>
        TrusteePartnershipEntity(partnership.PartnershipDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted, Some(SingleTrust.toString))
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

  private val isHnSEnabled = true
  private val mode = NormalMode

  private val company = CompanyDetails("test-company-name")
  private val person = PersonDetails("test-first-name", None, "test-last-name", LocalDate.now())
  private val partnershipDetails = PartnershipDetails("test-first-name")

  private val policyNumber = "Test policy number"
  private val insurerAddress = Address("addr1", "addr2", Some("addr3"), Some("addr4"), Some("xxx"), "GB")

  private val crn = CompanyRegistrationNumber.Yes("test-crn")
  private val utr = UniqueTaxReference.Yes("test-utr")
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYears = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")
  private val contactDetails = ContactDetails("test@test.com", "1234")

  private val stringValue = "aa"
  private val firstName = "First"
  private val lastName = "Last"

  private val insuranceCompanyDetails = UserAnswers().investmentRegulated(true).occupationalPensionScheme(true).
    typeOfBenefits(TypeOfBenefits.Defined).benefitsSecuredByInsurance(true).insuranceCompanyName(company.companyName).
    insurancePolicyNumber(policyNumber).insurerConfirmAddress(insurerAddress)

  private val trustee = insuranceCompanyDetails
    .trusteesCompanyDetails(0, company)
    .trusteesCompanyRegistrationNumber(0, crn)
    .trusteesUniqueTaxReference(0, utr)
    .trusteesCompanyAddress(0, address)
    .trusteesCompanyAddressYears(0, addressYears)
    .trusteesCompanyPreviousAddress(0, previousAddress)
    .set(CompanyVatId(0))(Vat.Yes("vat")).asOpt.value
    .set(CompanyPayeId(0))(Paye.Yes("vat")).asOpt.value

  private val userAnswersHnS = UserAnswers(readJsonFromFile("/payloadHnS.json"))

  val establisherPartnership = trustee.set(EstablisherKindId(0))(EstablisherKind.Partnership)
    .flatMap(_.set(PartnershipDetailsId(0))(PartnershipDetails("")))
    .flatMap(_.set(PartnershipVatId(0))(Vat.No))
    .flatMap(_.set(PartnershipPayeId(0))(Paye.No))
    .flatMap(_.set(PartnershipUniqueTaxReferenceID(0))(utr))
    .flatMap(_.set(PartnershipAddressId(0))(address))
    .flatMap(_.set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnershipPreviousAddressId(0))(previousAddress))
    .flatMap(_.set(PartnershipContactDetailsId(0))(contactDetails))
    .flatMap(_.set(PartnerDetailsId(0, 0))(PersonDetails("par1", None, "", LocalDate.now)))
    .asOpt.value
}
