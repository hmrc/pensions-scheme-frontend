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

package utils

import base.JsonFileReader
import helpers.DataCompletionHelper
import identifiers.register.establishers.company.director._
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.partnership.partner._
import identifiers.register.establishers.{EstablisherKindId, EstablishersId, IsEstablisherNewId}
import identifiers.register.trustees.company.{CompanyEnterPAYEId, CompanyEnterVATId, HasCompanyCRNId, HasCompanyPAYEId, HasCompanyUTRId, HasCompanyVATId, CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.{company => _, _}
import models._
import models.address.Address
import models.person._
import models.register.SchemeType.SingleTrust
import models.register._
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind.{Company, Indivdual, Partnership}
import models.register.trustees.TrusteeKind
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits with DataCompletionHelper with JsonFileReader {

  import UserAnswersSpec._

  ".allEstablishers" must {
    "return a sequence of establishers names, edit links and delete links" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payload.json"))

      val allEstablisherEntities: Seq[Establisher[_]] = Seq(
        establisherEntity("Test Company", 0, Company, isComplete = true),
        establisherEntity("Test Individual", 1, Indivdual, isComplete = true),
        establisherEntity("Test Partnership", 2, Partnership, isComplete = true)
      )

      userAnswers.allEstablishers(mode) mustEqual allEstablisherEntities
    }

    "return en empty sequence if there are no establishers" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allEstablishers(mode) mustEqual Seq.empty
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
      userAnswers.allEstablishers(mode) mustEqual Seq.empty
    }
  }

  ".allEstablishersAfterDelete" must {
    "return a map of establishers names, edit links and delete links when two of the establishers is deleted" in {
      val json = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            EstablisherNameId.toString ->
              PersonName("my", "name 1"),
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
            EstablisherNameId.toString ->
              PersonName("my", "name 3"),
            IsEstablisherNewId.toString -> true,
            EstablisherKindId.toString -> EstablisherKind.Indivdual.toString
          ),
          Json.obj(
            EstablisherCompanyDetailsId.toString ->
              CompanyDetails("my company 4", isDeleted = true),
            EstablisherKindId.toString -> EstablisherKind.Company.toString,
            IsEstablisherNewId.toString -> true
          )
        )
      )

      val userAnswers = UserAnswers(json)
      val allEstablisherEntities: Seq[Establisher[_]] =
        Seq(establisherEntity("my name 1", 0, Indivdual, countAfterDeleted = 2),
          establisherEntity("my name 3", 2, Indivdual, countAfterDeleted = 2))

      userAnswers.allEstablishersAfterDelete(mode) mustEqual allEstablisherEntities
    }
  }

  ".allTrustees" must {
    "return a map of trustee names, edit links, delete links and isComplete flag" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payload.json"))

      val allTrusteesEntities: Seq[Trustee[_]] = Seq(
        trusteeEntity("test company", 0, TrusteeKind.Company, isComplete = true),
        trusteeEntity("firstName lastName", 1, TrusteeKind.Individual, isComplete = true),
        trusteeEntity("test partnership", 2, TrusteeKind.Partnership, isComplete = true)
      )

      val result = userAnswers.allTrustees

      result mustEqual allTrusteesEntities
    }

    "return en empty sequence if there are no trustees " in {
      val json = Json.obj(
        TrusteesId.toString -> Json.arr(
        )
      )
      val userAnswers = UserAnswers(json)
      userAnswers.allTrustees mustEqual Seq.empty
    }


    "return en empty sequence if the json is invalid " in {
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
            TrusteeNameId.toString -> PersonName("First", "Last", isDeleted = true),
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Company.toString,
            identifiers.register.trustees.company.CompanyDetailsId.toString -> CompanyDetails("My Company"),
            IsTrusteeNewId.toString -> true
          ),
          Json.obj(
            TrusteeKindId.toString -> TrusteeKind.Individual.toString,
            TrusteeNameId.toString -> PersonName("FName", "LName", isDeleted = true),
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

      val result = userAnswers.allTrusteesAfterDelete

      result mustEqual allTrusteesEntities
    }
  }

  ".allDirectors" must {

    "return a map of director names, edit links, delete links and isComplete flag including deleted items where names are all the same" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payload.json"))
        .set(DirectorNameId(0, 1))(PersonName("First", "Last", isDeleted = true)).asOpt.value
        .set(DirectorNameId(0, 2))(PersonName("First", "Last")).asOpt.value

      val directorEntities = Seq(
        DirectorEntity(DirectorNameId(0, 0), "Director One", isDeleted = false, isCompleted = true, isNewEntity = true, 2),
        DirectorEntity(DirectorNameId(0, 1), "First Last", isDeleted = true, isCompleted = false, isNewEntity = false, 2),
        DirectorEntity(DirectorNameId(0, 2), "First Last", isDeleted = false, isCompleted = false, isNewEntity = false, 2))

      val result = userAnswers.allDirectors(0)

      result.size mustEqual 3
      result mustBe directorEntities
    }
  }

  ".allDirectorsAfterDelete" must {

    "return a map of director names, edit links and delete links after one of the directors is deleted" in {
      val userAnswers = UserAnswers()
        .set(DirectorNameId(0, 0))(PersonName("First", "Last", isDeleted = true))
        .flatMap(_.set(DirectorNameId(0, 1))(PersonName("First1", "Last1"))).get

      val directorEntities = Seq(
        DirectorEntity(DirectorNameId(0, 1), "First1 Last1", isDeleted = false, isCompleted = false, isNewEntity = false, 1))
      val result = userAnswers.allDirectorsAfterDelete(0)

      result.size mustEqual 1
      result mustBe directorEntities
    }
  }

  ".allPartners" must {

    "return a map of partner names, edit links, delete links and isComplete flag including deleted items where names are all the same" in {
      val userAnswers = UserAnswers(readJsonFromFile("/payload.json"))
        .set(PartnerNameId(2, 2))(PersonName("Partner", "One", isDeleted = true))
        .flatMap(_.set(PartnerNameId(2, 3))(PersonName("Partner", "One")))
        .flatMap(_.set(IsNewPartnerId(2, 0))(true))
        .get

      val partnerEntities = Seq(
        PartnerEntity(PartnerNameId(2, 0), "Partner One", isDeleted = false, isCompleted = true, isNewEntity = true, 3),
        PartnerEntity(PartnerNameId(2, 1), "Partner Two", isDeleted = false, isCompleted = true, isNewEntity = true, 3),
        PartnerEntity(PartnerNameId(2, 2), "Partner One", isDeleted = true, isCompleted = false, isNewEntity = false, 3),
        PartnerEntity(PartnerNameId(2, 3), "Partner One", isDeleted = false, isCompleted = false, isNewEntity = false, 3))

      val result = userAnswers.allPartners(2)

      result.size mustEqual 4
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
            EstablisherNameId.toString ->
              PersonName("my", "name", isDeleted = true)
          ),
          Json.obj(
            EstablisherNameId.toString ->
              PersonName("my", "name")
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
        .set(TrusteeNameId(0))(PersonName("First", "Last", isDeleted = true))
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
          .set(EstablisherNameId(0))(person).asOpt.value
          .set(EstablisherKindId(0))(EstablisherKind.Indivdual).asOpt.value
          .set(EstablisherKindId(1))(EstablisherKind.Company)
          .flatMap(_.set(EstablisherCompanyDetailsId(1))(company))
          .asOpt
          .value

      answers.hasCompanies(mode) mustBe true
    }

    "return false if no establishers are companies" in {
      val answers =
        UserAnswers()
          .set(EstablisherNameId(0))(person)
          .flatMap(_.set(TrusteeNameId(0))(person))
          .asOpt
          .value

      answers.hasCompanies(mode) mustBe false
    }

    "return false if there are no establishers or trustees" in {
      val answers =
        UserAnswers()

      answers.hasCompanies(mode) mustBe false
    }
  }

  "areVariationChangesCompleted" when {
    "checking insurance company" must {
       "return false if scheme have insurance and details are missing" in {
         val insuranceCompanyDetails = UserAnswers().investmentRegulated(true)
         insuranceCompanyDetails.areVariationChangesCompleted mustBe false
       }

       "return false if scheme have insurance is not defined" in {
         val insuranceCompanyDetails = UserAnswers()
         insuranceCompanyDetails.areVariationChangesCompleted mustBe false
       }

       "return true if scheme does not have insurance" in {
         val insuranceCompanyDetails = UserAnswers().benefitsSecuredByInsurance(false)
         insuranceCompanyDetails.areVariationChangesCompleted mustBe true
       }

       "return true if scheme have insurance and all the details are present" in {
         insuranceCompanyDetails.areVariationChangesCompleted mustBe true
       }
     }

    "checking trustees" must {
      "return true if trustees are not defined" in {
        insuranceCompanyDetails.areVariationChangesCompleted mustBe true
      }

      "return false if trustees are not completed" in {
        trustee.areVariationChangesCompleted mustBe false
      }

      "return true if trustees are completed" in {
        val trusteeCompleted = trustee.trusteesCompanyPhone(0, "12345")
            .trusteesCompanyEmail(0, "z@z.z")
        trusteeCompleted.areVariationChangesCompleted mustBe true
      }
    }

    "checking establishers" must {
      val userAnswersInprogress = UserAnswers(readJsonFromFile("/payloadInProgress.json"))
      val userAnswersCompleted = UserAnswers(readJsonFromFile("/payload.json"))
      "return false if establishers are not completed" in {
        userAnswersInprogress.areVariationChangesCompleted mustBe false
      }

      "return true if establishers are completed " in {
        userAnswersCompleted.areVariationChangesCompleted mustBe true
      }
    }
  }

}

object UserAnswersSpec extends OptionValues with Enumerable.Implicits with JsonFileReader {
  private def establisherEntity(name: String, index: Int, establisherKind: EstablisherKind,
                                isComplete: Boolean = false, countAfterDeleted : Int = 3): Establisher[_] = {
    establisherKind match {
      case Indivdual =>
        EstablisherIndividualEntity(EstablisherNameId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted)
      case Company =>
        EstablisherCompanyEntity(EstablisherCompanyDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted)
      case _ =>
        EstablisherPartnershipEntity(PartnershipDetailsId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted)
    }
  }

  private def trusteeEntity(name: String, index: Int, trusteeKind: TrusteeKind, isComplete: Boolean = false, countAfterDeleted : Int = 3): Trustee[_] = {
    trusteeKind match {
      case TrusteeKind.Individual =>
        TrusteeIndividualEntity(TrusteeNameId(index), name, isDeleted = false, isCompleted = isComplete, isNewEntity = true, countAfterDeleted, Some(SingleTrust.toString))
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
  private val mode = NormalMode

  private val company = CompanyDetails("test-company-name")
  private val person = PersonName("test-first-name",  "test-last-name")
  private val partnershipDetails = PartnershipDetails("test-first-name")

  private val policyNumber = "Test policy number"
  private val insurerAddress = Address("addr1", "addr2", Some("addr3"), Some("addr4"), Some("xxx"), "GB")

  private val newCrn = "test-crn"
  private val newUtr = "test-utr"

  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYears = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val stringValue = "aa"
  private val firstName = "First"
  private val lastName = "Last"

  private val insuranceCompanyDetails = UserAnswers().investmentRegulated(true).occupationalPensionScheme(true).
    typeOfBenefits(TypeOfBenefits.Defined).benefitsSecuredByInsurance(true).insuranceCompanyName(company.companyName).
    insurancePolicyNumber(policyNumber).insurerConfirmAddress(insurerAddress)

  private val trustee = insuranceCompanyDetails
    .trusteesCompanyDetails(0, company)
    .set(HasCompanyCRNId(0))(true).asOpt.value
    .trusteesCompanyEnterCRN(0, ReferenceValue(newCrn))
    .set(HasCompanyUTRId(0))(true).asOpt.value
    .trusteesCompanyEnterUTR(0, ReferenceValue(newUtr))
    .trusteesCompanyAddress(0, address)
    .trusteesCompanyAddressYears(0, addressYears)
    .trusteesCompanyPreviousAddress(0, previousAddress)
    .set(HasCompanyVATId(0))(true).asOpt.value
    .set(CompanyEnterVATId(0))(ReferenceValue("vat")).asOpt.value
    .set(HasCompanyPAYEId(0))(true).asOpt.value
    .set(CompanyEnterPAYEId(0))(ReferenceValue("vat")).asOpt.value
}
