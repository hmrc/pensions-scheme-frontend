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
import play.api.data.{Form, FormError}

class BankAccountDetailsFormProviderSpec extends FormBehaviours with Constraints with BankDetailsBehaviour {


  override def formWithSortCode[T](testForm: Form[T],
                                   keyRequired: String,
                                   keyLengthError: String,
                                   keyInvalid: String,
                                   validOtherData: Map[String, String],
                                   getSortCode: T => SortCode
                                  ): Unit = {


    "behave like form with SortCode" should {

      s"bind a valid sort code" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode.first" -> "12", "sortCode.second" -> "34", "sortCode.third" -> "56"))
        getSortCode(result.get) shouldBe SortCode("12", "34", "56")
      }

      "not bind when sort code is not supplied" in {
        val result = testForm.bind(validOtherData)
        result.errors shouldBe Seq(FormError("sortCode", keyRequired))
      }

      s"not bind an invalid sort code using non-alpha chars" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode.first" -> "1%", "sortCode.second" -> "3&", "sortCode.third" -> "56"))
        result.errors shouldBe Seq(FormError("sortCode", keyInvalid))
      }

      s"not bind an invalid sort code using alpha chars" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode.first" -> "ab", "sortCode.second" -> "cd", "sortCode.third" -> "ef"))
        result.errors shouldBe Seq(FormError("sortCode", keyInvalid))
      }

      s"not bind a sort code which exceeds max length" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode.first" -> "11", "sortCode.second" -> "22", "sortCode.third" -> "333"))
        result.errors shouldBe Seq(FormError("sortCode", keyLengthError))
      }

      s"not bind a sort code which is less than expected length" in {
        val result = testForm.bind(validOtherData ++ Map("sortCode.first" -> "11", "sortCode.second" -> "22", "sortCode.third" -> "3"))
        result.errors shouldBe Seq(FormError("sortCode", keyLengthError))
      }
    }
  }

  //scalastyle:off magic.number

  val testSortCode = SortCode("24", "56", "56")
  val testAccountNumber: String = RandomUtils.nextInt(10000000, 99999999).toString

  private val day = LocalDate.now().getDayOfMonth
  private val month = LocalDate.now().getMonthOfYear
  private val year = LocalDate.now().getYear

  val validData: Map[String, String] = Map(
    "bankName" -> "test bank",
    "accountName" -> "test account",
    "sortCode.first" -> "24",
    "sortCode.second" -> "56",
    "sortCode.third" -> "56",
    "accountNumber" -> testAccountNumber
  )

  val bankDetails = BankAccountDetails("test bank", "test account",
    testSortCode, testAccountNumber)

  val form = new BankAccountDetailsFormProvider()()


  "BankAccountDetails form" must {

    behave like formWithMandatoryTextFields(
      Field("bankName", Required -> "messages__error__bank_name__blank"),
      Field("accountName", Required -> "messages__error__bank_account_holder_name__blank"),
      Field("accountNumber", Required -> "messages__error__bank_accno__blank")
    )

    behave like formWithSortCode(
      form,
      "messages__error__sort_code__blank",
      "messages__error__sort_code__length",
      "messages__error__sort_code__invalid",
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

    Seq("Abffgk", "1 1 1 1 1 1 ", "A1b3j4b2").foreach { code =>
      s"fail to bind when account number $code is invalid" in {
        val data = validData + ("accountNumber" -> code)

        val expectedError = error("accountNumber", "messages__error__bank_accno__invalid", regexAccountNo)
        checkForError(form, data, expectedError)
      }
    }

    "fail to bind when account number is less than length 8" in {
      val data = validData + ("accountNumber" -> "1234567")

      val expectedError = error("accountNumber", "messages__error__bank_accno__length", 8)
      checkForError(form, data, expectedError)
    }

    "fail to bind when account number exceeds length 8" in {
      val data = validData + ("accountNumber" -> "123456789")

      val expectedError = error("accountNumber", "messages__error__bank_accno__length", 8)
      checkForError(form, data, expectedError)
    }
  }
}
