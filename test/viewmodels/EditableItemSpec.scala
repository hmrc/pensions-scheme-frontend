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

import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.{CompanyDetails, NormalMode}
import models.person.PersonDetails
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
        controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0).url
      ),
      EditableItem(
        1,
        trusteeDetails.fullName,
        controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(1, TrusteeKind.Individual).url,
        controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 1).url
      )
    )

    val actual = EditableItem.fromTrusteeEntityDetails(trustees)
    actual shouldBe expected
  }

}
