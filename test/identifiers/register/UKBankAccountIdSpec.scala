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

import models.register.{SortCode, UKBankDetails}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class UKBankAccountIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(UKBankAccountId)(true)
      .flatMap(_.set(UKBankDetailsId)(UKBankDetails("test bank name", "test account name",
        SortCode("11", "11", "11"), "test account number", LocalDate.now)))
      .asOpt.value

    "`UKBankAccountId` is set to `false`" when {

      val result: UserAnswers = answers.set(UKBankAccountId)(false).asOpt.value

      "remove the data for `UBbankDetails`" in {
        result.get(UKBankDetailsId) mustNot be(defined)
      }
    }

    "`UKBankAccountId` is set to `true`" when {

      val result: UserAnswers = answers.set(UKBankAccountId)(true).asOpt.value

      "not remove the data for `UBbankDetails`" in {
        result.get(UKBankDetailsId) mustBe defined
      }
    }
  }
}
