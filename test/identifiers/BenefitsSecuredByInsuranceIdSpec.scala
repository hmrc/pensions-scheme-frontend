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

package identifiers

import models.address.{Address, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class BenefitsSecuredByInsuranceIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answers = UserAnswers(Json.obj())
      .set(BenefitsSecuredByInsuranceId)(true)
      .flatMap(_.set(InsuranceCompanyNameId)("test"))
      .flatMap(_.set(InsurancePolicyNumberId)("test"))
      .flatMap(_.set(InsurerEnterPostCodeId)(Seq.empty))
      .flatMap(_.set(InsurerSelectAddressId)(TolerantAddress(None, None, None, None, None, None)))
      .flatMap(_.set(InsurerConfirmAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`BenefitsSecuredByInsuranceId` is set to `false`" must {

      val result: UserAnswers = answers.set(BenefitsSecuredByInsuranceId)(false).asOpt.value

      "remove the data for `InsuranceCompanyName`" in {
        result.get(InsuranceCompanyNameId) mustNot be(defined)
      }

      "remove the data for `InsurancePolicyNumber`" in {
        result.get(InsurancePolicyNumberId) mustNot be(defined)
      }

      "remove the data for `InsurerEnterPostCode`" in {
        result.get(InsurerEnterPostCodeId) mustNot be(defined)
      }

      "remove the data for `InsurerSelectAddress`" in {
        result.get(InsurerSelectAddressId) mustNot be(defined)
      }

      "remove the data for `InsurerConfirmAddress`" in {
        result.get(InsurerConfirmAddressId) mustNot be(defined)
      }
    }

    "`BenefitsSecuredByInsuranceId` is set to `true`" must {

      val result: UserAnswers = answers.set(BenefitsSecuredByInsuranceId)(true).asOpt.value

      "set the IsAboutSectionComplete to false" in {
        result.get(IsAboutBenefitsAndInsuranceCompleteId).value mustBe false
      }

      "not remove the data for `InsuranceCompanyName`" in {
        result.get(InsuranceCompanyNameId) must be(defined)
      }

      "not remove the data for `InsurancePolicyNumber`" in {
        result.get(InsurancePolicyNumberId) must be(defined)
      }

      "not remove the data for `InsurerEnterPostCode`" in {
        result.get(InsurerEnterPostCodeId) must be(defined)
      }

      "not remove the data for `InsurerSelectAddress`" in {
        result.get(InsurerSelectAddressId) must be(defined)
      }

      "not remove the data for `InsurerConfirmAddress`" in {
        result.get(InsurerConfirmAddressId) must be(defined)
      }
    }
  }
}
