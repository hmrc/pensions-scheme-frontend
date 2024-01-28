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

package forms

import forms.behaviours.{BankDetailsBehaviour, FormBehaviours}
import forms.mappings.Constraints
import models._
import models.register.SortCode
import org.apache.commons.lang3.RandomUtils

class BankAccountDetailsFormProviderSpec extends FormBehaviours with Constraints with BankDetailsBehaviour {

  //scalastyle:off magic.number

  val testSortCode = SortCode("24", "56", "56")
  val testAccountNumber: String = RandomUtils.nextInt(10000000, 99999999).toString

  val validData: Map[String, String] = Map(
    "sortCode" -> "24 56 56",
    "accountNumber" -> testAccountNumber
  )

  val bankDetails = BankAccountDetails(testSortCode, testAccountNumber)

  val form = new BankAccountDetailsFormProvider()()


  "BankAccountDetails form" must {

    behave like formWithMandatoryTextFields(
      Field("sortCode", Required -> "messages__error__sort_code__blank"),
      Field("accountNumber", Required -> "messages__error__bank_accno__blank")
    )

    behave like formWithSortCodeHS(
      form,
      "messages__error__sort_code__blank",
      "messages__error__sort_code__length",
      Map(
        "bankName" -> "test bank",
        "accountName" -> "test account",
        "accountNumber" -> testAccountNumber
      ),
      (bankDetails: BankAccountDetails) => bankDetails.sortCode
    )

    behave like formWithAccountNumber(
      form,
      "messages__error__bank_accno__blank",
      "messages__error__bank_accno__invalid",
      Map(
        "bankName" -> "test bank",
        "accountName" -> "test account",
        "sortCode" -> "123456"
      ),
      (bankDetails: BankAccountDetails) => bankDetails.accountNumber
    )

  }
}
