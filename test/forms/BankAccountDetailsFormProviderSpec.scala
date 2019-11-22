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

package forms

import forms.behaviours.{BankDetailsBehaviour, FormBehaviours}
import forms.mappings.Constraints
import models._
import models.register.SortCode
import org.apache.commons.lang3.{RandomStringUtils, RandomUtils}
import org.joda.time.LocalDate

class BankAccountDetailsFormProviderSpec extends FormBehaviours with Constraints with BankDetailsBehaviour {

  //scalastyle:off magic.number

  val testSortCode = SortCode("24", "56", "56")
  val testAccountNumber: String = RandomUtils.nextInt(10000000, 99999999).toString

  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear

  val validData: Map[String, String] = Map(
    "bankName" -> "test bank",
    "accountName" -> "test account",
    "sortCode" -> "24 56 56",
    "accountNumber" -> testAccountNumber
  )

  val bankDetails = BankAccountDetails("test bank", "test account",
    testSortCode, testAccountNumber)

  val form = new BankAccountDetailsFormProvider()()


  "BankAccountDetails form" must {

    behave like formWithMandatoryTextFields(
      Field("bankName", Required -> "messages__error__bank_name__blank"),
      Field("accountName", Required -> "messages__error__bank_account_holder_name__blank"),
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

    "fail to bind when the bank name exceeds max length 28" in {
      val testString = RandomStringUtils.random(29)
      val data = validData + ("bankName" -> testString)

      val expectedError = error("bankName", "messages__error__bank_name__length", 28)
      checkForError(form, data, expectedError)
    }

    "fail to bind when account name exceeds max length 28" in {
      val testString = RandomStringUtils.random(29)
      val data = validData + ("accountName" -> testString)

      val expectedError = error("accountName", "messages__error__bank_account_holder_name__length", 28)
      checkForError(form, data, expectedError)
    }

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
