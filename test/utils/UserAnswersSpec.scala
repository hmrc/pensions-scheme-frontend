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
import models.register.establishers.EstablisherKind
import models.register.establishers.EstablisherKind.{Company, Indivdual}
import models.register.establishers.company.director.DirectorDetails
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.Individual
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._
import viewmodels.EditableItem

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues {
 import UserAnswersSpec._
  ".allEstablishers" must {
    "return a map of establishers names, edit links and delete links" in {
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
      val allEstablishersEditableItem = Seq(editableItem("my company", 0, Company), editableItem("my name", 1, Indivdual))

      userAnswers.allEstablishers mustEqual allEstablishersEditableItem
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
              CompanyDetails("my company 2", None, None, true)
          ),
          Json.obj(
            EstablisherDetailsId.toString ->
              PersonDetails("my", None, "name 3", LocalDate.now)
          )
        )
      )

      val userAnswers = UserAnswers(json)
      val allEstablisherEditableItems = Seq(editableItem("my name 1", 0, Indivdual), editableItem("my name 3", 2, Indivdual))

      userAnswers.allEstablishersAfterDelete mustEqual allEstablisherEditableItems
    }
  }

  ".allTrustees" must {

    "return a map of trustee names, edit links and delete links" in {
      val userAnswers = UserAnswers()
        .set(TrusteeDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now))
        .flatMap(_.set(identifiers.register.trustees.company.CompanyDetailsId(1))(CompanyDetails("My Company", None, None))).get
      val allTrusteesEditableItem = Seq(trusteeEditableItem("First Last", 0, TrusteeKind.Individual), trusteeEditableItem("My Company", 1, TrusteeKind.Company))

      val result = userAnswers.allTrustees

      result mustEqual allTrusteesEditableItem
    }
  }

  ".allTrusteesAfterDelete" must {

    "return a map of trustee names, edit links and delete links when one of the trustee is deleted" in {
      val userAnswers = UserAnswers()
        .set(TrusteeDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now, true))
        .flatMap(_.set(identifiers.register.trustees.company.CompanyDetailsId(1))(CompanyDetails("My Company", None, None))).get
      val allTrusteesEditableItem = Seq(trusteeEditableItem("My Company", 1, TrusteeKind.Company))

      val result = userAnswers.allTrusteesAfterDelete

      result mustEqual allTrusteesEditableItem
    }
  }

  ".allDirectors" must {

    "return a map of director names, edit links and delete links" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(DirectorDetails("First", None, "Last", LocalDate.now, false))
        .flatMap(_.set(DirectorDetailsId(0, 1))(DirectorDetails("First1", None, "Last1", LocalDate.now, false))).get
      val directorEditableItem = Seq(
        EditableItem(0, "First Last", false, editDirectorLink(0, 0), deleteDirectorLink(0, 0)),
        EditableItem(1, "First1 Last1", false, editDirectorLink(1, 0), deleteDirectorLink(1, 0)))
      val result = userAnswers.allDirectors(0)

      result.size mustEqual 2
      result mustBe directorEditableItem
    }
  }

  ".allDirectorsAfterDelete" must {

    "return a map of director names, edit links and delete links after one of the directors is deleted" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0, 0))(DirectorDetails("First", None, "Last", LocalDate.now, true))
        .flatMap(_.set(DirectorDetailsId(0, 1))(DirectorDetails("First1", None, "Last1", LocalDate.now, false))).get
      val directorEditableItem = Seq(
        EditableItem(1, "First1 Last1", false, editDirectorLink(1, 0), deleteDirectorLink(1, 0)))
      val result = userAnswers.allDirectorsAfterDelete(0)

      result.size mustEqual 1
      result mustBe directorEditableItem
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
              PersonDetails("my", None, "name", LocalDate.now, true)
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
        .set(TrusteeDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now, true))
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
  private def deleteDirectorLink(index: Int, establisherIndex: Int) = controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(establisherIndex, index).url
  private def editDirectorLink(index: Int, establisherIndex: Int) = controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, establisherIndex, index).url

  private def editableItem(name: String, index: Int, establisherKind: EstablisherKind) = {
    establisherKind match {
      case Indivdual =>
        EditableItem(index, name, false, controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, index).url,
          controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(index, Indivdual).url)
      case _ =>
        EditableItem(index, name, false, controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url,
          controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(index, Company).url)
    }
  }

  private def trusteeEditableItem(name: String, index: Int, trusteeKind: TrusteeKind) = {
    trusteeKind match {
      case Individual =>
        EditableItem(index, name, false, controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, index).url,
          controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(index, TrusteeKind.Individual).url)
      case _ =>
        EditableItem(index, name, false, controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index).url,
          controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(index, TrusteeKind.Company).url)
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