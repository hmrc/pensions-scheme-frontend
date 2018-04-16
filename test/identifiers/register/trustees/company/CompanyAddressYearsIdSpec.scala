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

package identifiers.register.trustees.company

import models.AddressYears
import models.address.Address
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class CompanyAddressYearsIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(Seq.empty))
      .flatMap(_.set(CompanyPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" when {

      val result: UserAnswers = answers.set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostcodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId(0)) mustNot be(defined)
      }
    }

    "`AddressYears` is set to `UnderAYear`" when {

      val result: UserAnswers = answers.set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostcodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId(0)) mustBe defined
      }
    }

    "`AddressYears` is removed" when {

      val result: UserAnswers = answers.remove(CompanyAddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostcodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId(0)) mustBe defined
      }
    }
  }
}
