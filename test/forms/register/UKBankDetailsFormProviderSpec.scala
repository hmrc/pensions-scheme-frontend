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

package forms.register

import forms.behaviours.FormBehaviours
import models.{Date, Field, Required, UKBankDetails}
import org.apache.commons.lang3.{RandomStringUtils, RandomUtils}
import org.joda.time.LocalDate

class UKBankDetailsFormProviderSpec extends FormBehaviours {

  val testSortCode: String = RandomUtils.nextInt(100000, 999999).toString
  val testAccountNumber: String = RandomUtils.nextInt(10000000, 99999999).toString

  val validData: Map[String, String] = Map(
    "bankName" -> "test bank",
    "accountName" -> "test account",
    "sortCode" -> testSortCode,
    "accountNumber" -> testAccountNumber,
    "date.day" -> "1",
    "date.month" -> "1",
    "date.year" -> LocalDate.now().getYear.toString
  )

  val bankDetails = UKBankDetails("test bank", "test account",
    testSortCode, testAccountNumber, Date(1, 1, LocalDate.now().getYear))

  val form = new UKBankDetailsFormProvider()()


  "UKBankDetails form" must {
    behave like questionForm(bankDetails)

    behave like formWithMandatoryTextFields(
      Field("bankName", Required -> "messages__error__bank_name"),
      Field("accountName", Required -> "messages__error__account_name"),
      Field("sortCode", Required -> "messages__error__sort_code"),
      Field("accountNumber", Required -> "messages__error__account_number"),
      Field("date.day", Required -> "messages__error__date"),
      Field("date.month", Required -> "messages__error__date"),
      Field("date.year", Required -> "messages__error__date")
    )

    "fail to bind when the bank name exceeds max length 28" in {
      val testString = RandomStringUtils.random(29)
      val data = validData + ("bankName" -> testString)

      val expectedError = error("bankName", "messages__error__bank_name_length", 28)
      checkForError(form, data, expectedError)
    }

    "fail to bind when account name exceeds max length 28" in {
      val testString = RandomStringUtils.random(29)
      val data = validData + ("accountName" -> testString)

      val expectedError = error("accountName", "messages__error__account_name_length", 28)
      checkForError(form, data, expectedError)
    }

    Seq("12$12£14", "asdcffdsf", "11 23%12").foreach { code =>
      s"fail to bind when sort code $code is invalid" in {
        val regexSortCode = """[0-9 -]*""".r.toString()
        val data = validData + ("sortCode" -> code)

        val expectedError = error("sortCode", "messages__error__sort_code_invalid", regexSortCode)
        checkForError(form, data, expectedError)
      }
    }

    Seq("1234567", "12 45 67 78", "12-56-90-0").foreach { code =>
      s"fail to bind when sort code $code exceeds max length 6" in {
        val data = validData + ("sortCode" -> code)

        val expectedError = error("sortCode", "messages__error__sort_code_length", 6)
        checkForError(form, data, expectedError)
      }
    }

    Seq("Abffgk", "1 1 1 1 1 1 ", "A1b3j4b2").foreach { code =>
      s"fail to bind when account number $code is invalid" in {
        val regexAccountNo = """[0-9]*""".r.toString()
        val data = validData + ("accountNumber" -> code)

        val expectedError = error("accountNumber", "messages__error__account_number_invalid", regexAccountNo)
        checkForError(form, data, expectedError)
      }
    }

    "fail to bind when account number exceeds max length 8" in {
      val data = validData + ("accountNumber" -> "123456789")

      val expectedError = error("accountNumber", "messages__error__account_number_length", 8)
      checkForError(form, data, expectedError)
    }
  }
}
