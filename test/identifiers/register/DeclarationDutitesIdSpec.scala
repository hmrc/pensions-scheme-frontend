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

package identifiers.register

import identifiers.register.adviser.{AdviserAddressId, AdviserAddressPostCodeLookupId, AdviserDetailsId}
import models.address.Address
import models.register.AdviserDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class DeclarationDutitesIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "cleanup" must {

    "`DeclarationDuties` changed from false to true" when {
      val answers = UserAnswers(Json.obj())
        .set(DeclarationDutiesId)(false)
        .flatMap(_.set(AdviserDetailsId)(AdviserDetails("name", "email")))
        .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq.empty))
        .flatMap(_.set(AdviserAddressId)(Address("", "", None, None, None, ""))).asOpt.value

      val result = answers.set(DeclarationDutiesId)(true).asOpt.value

      "remove the data for `adviser details`" in {
        result.get(AdviserDetailsId) mustNot be(defined)
      }
      "remove the data for `adviser address`" in {
        result.get(AdviserAddressPostCodeLookupId) mustNot be(defined)
        result.get(AdviserAddressId) mustNot be(defined)
      }
    }
  }
}
