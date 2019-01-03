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

package identifiers.register.trustees.individual

import identifiers.register.trustees.MoreThanTenTrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import models.CompanyDetails
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class TrusteeDetailsIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  private def individualTrustee(index: Int) = PersonDetails(
    s"test-trustee-$index",
    None,
    "test-last-name",
    LocalDate.now()
  )

  private def companyTrustee(index: Int) = CompanyDetails(
    s"test-company-$index",
    None,
    None
  )

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(CompanyDetailsId(0))(companyTrustee(0))
      .flatMap(_.set(TrusteeDetailsId(1))(individualTrustee(1)))
      .flatMap(_.set(MoreThanTenTrusteesId)(true))
      .asOpt.value

    "One trustee is deleted from a set of 10 while the `more than ten trustees` flag was set to yes" when {

      val result: UserAnswers = answers.remove(TrusteeDetailsId(1)).asOpt.value

      "remove the data for `More than 10 trustees`" in {
        result.get(MoreThanTenTrusteesId) mustNot be(defined)
      }


    }

  }

}
