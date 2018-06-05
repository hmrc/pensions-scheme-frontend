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

package viewmodels

import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.{CheckMode, CompanyDetails}
import models.person.PersonDetails
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import org.joda.time.LocalDate
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import utils.UserAnswers

class EditableItemSpec extends FlatSpec with Matchers with OptionValues {

  "EditableItem" should "convert from trustee entitiy details" in {
    val companyDetails = CompanyDetails(
      "test-company",
      None,
      None
    )

    val trusteeDetails = PersonDetails(
      "John",
      None,
      "Doe",
      LocalDate.now()
    )

    val userAnswers =
      UserAnswers()
        .set(CompanyDetailsId(0))(companyDetails)
        .flatMap(_.set(TrusteeDetailsId(1))(trusteeDetails))
        .asOpt
        .value

    val trustees = userAnswers.allTrustees

    val expected = Seq(
      EditableItem(
        0,
        companyDetails.companyName,
        controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(0, TrusteeKind.Company).url,
        controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(CheckMode, 0).url
      ),
      EditableItem(
        1,
        trusteeDetails.fullName,
        controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(1, TrusteeKind.Individual).url,
        controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(CheckMode, 1).url
      )
    )

    val actual = EditableItem.fromEntityDetails(trustees.map(t => (t._1, t._2, EntityKind.Trustee)))
    actual shouldBe expected
  }

  "EditableItem" should "convert from establisher entity details" in {
    val companyDetails = CompanyDetails(
      "test-company",
      None,
      None
    )

    val establisherDetails = PersonDetails(
      "John",
      None,
      "Doe",
      LocalDate.now()
    )

    val userAnswers =
      UserAnswers()
        .set(identifiers.register.establishers.company.CompanyDetailsId(0))(companyDetails)
        .flatMap(_.set(EstablisherDetailsId(1))(establisherDetails))

    println(userAnswers)

    val establishers = userAnswers.asOpt.value.allEstablishers

    val expected = Seq(
      EditableItem(
        0,
        companyDetails.companyName,
        controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(0, EstablisherKind.Company).url,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, 0).url
      ),
      EditableItem(
        1,
        establisherDetails.fullName,
        controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(1, EstablisherKind.Indivdual).url,
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, 1).url
      )
    )

    val actual = EditableItem.fromEntityDetails(establishers.map(x => (x._1, x._2, EntityKind.Establisher)))
    actual shouldBe expected
  }
}
