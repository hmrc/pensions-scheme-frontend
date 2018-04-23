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

package identifiers.register.trustees

import identifiers.register._
import models.address.Address
import models.register.BenefitsInsurer
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class SecuredBenefitsIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answers = UserAnswers(Json.obj())
      .set(SecuredBenefitsId)(true)
      .flatMap(_.set(BenefitsInsurerId)(BenefitsInsurer("test", "test")))
      .flatMap(_.set(InsurerPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(InsurerAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`SecuredBenefitsId` is set to `false`" must {

      val result: UserAnswers = answers.set(SecuredBenefitsId)(false).asOpt.value

      "remove the data for `BenefitsInsurer`" in {
        result.get(BenefitsInsurerId) mustNot be(defined)
      }

      "remove the data for `InsurerAddressPostCodeLookup`" in {
        result.get(InsurerPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `InsurerAddress`" in {
        result.get(InsurerAddressId) mustNot be(defined)
      }
    }

    "`SecuredBenefitsId` is set to `true`" must {

      val result: UserAnswers = answers.set(SecuredBenefitsId)(true).asOpt.value

      "not remove the data for `BenefitsInsurer`" in {
        result.get(BenefitsInsurerId) mustBe defined
      }

      "not remove the data for `InsurerAddressPostCodeLookup`" in {
        result.get(InsurerPostCodeLookupId) mustBe defined
      }

      "not remove the data for `InsurerAddress`" in {
        result.get(InsurerAddressId) mustBe defined
      }
    }
  }
}
