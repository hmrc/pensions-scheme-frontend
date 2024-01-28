/*
 * Copyright 2024 HM Revenue & Customs
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

import models.BankAccountDetails
import models.register.SortCode
import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class UKBankAccountIdSpec extends AnyWordSpec with Matchers with OptionValues with Enumerable.Implicits {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(UKBankAccountId)(true)
      .flatMap(_.set(BankAccountDetailsId)(BankAccountDetails(SortCode("11", "11", "11"), "test account number")))
      .asOpt.value

    "`UKBankAccountId` is set to `false`" when {

      val result: UserAnswers = answers.set(UKBankAccountId)(false).asOpt.value

      "remove the data for `BankAccountDetailsId`" in {
        result.get(BankAccountDetailsId) mustNot be(defined)
      }
    }

  }
}
