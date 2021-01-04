/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.mappings



import java.time.Month

import java.time.LocalDate
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.{Invalid, Valid}
import utils.{CountryOptions, InputOption}

// scalastyle:off magic.number

class ConstraintsSpec extends PlaySpec with Constraints with RegexBehaviourSpec {

  "firstError" must {

    "return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result mustEqual Valid
    }

    "return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" must {

    "return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" must {

    "return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }

  "regexp" must {

    "return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" must {

    "return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }


  "country" must {

    val keyInvalid = "error.invalid"

    val countryOptions: CountryOptions = new CountryOptions(
      Seq(
        InputOption("GB", "United Kingdom"),
        InputOption("PN", "Ponteland")
      )
    )

    "return valid when the country code exists" in {
      val result = country(countryOptions, keyInvalid).apply("GB")
      result mustBe Valid
    }

    "return invalid when the country code does not exist" in {
      val result = country(countryOptions, keyInvalid).apply("XXX")
      result mustBe Invalid(keyInvalid)
    }
  }

  "validCrn" must {

    Seq("1234567", "12345678", "ABCDEFG", "abcdefgh", "abc def", "abd-defg").foreach { crn =>
      s"return Valid for a string ($crn) which meets the CRN validity requirements" in {
        val result = validCrn("error.invalid")(crn)
        result mustEqual Valid
      }
    }

    Seq("123456", "123456789", "abc.def").foreach { crn =>
      s"return Invalid for a string ($crn) which doesn't meet the CRN validity requirements" in {
        val result = validCrn("error.invalid")(crn)
        result mustEqual Invalid("error.invalid")
      }
    }
  }

  "name" must {

    val validName = Table(
      "name",
      "a`",
      "a.",
      "a'",
      "a&",
      "a",
      "a-",
      "A B",
      "a^"
    )

    val invalidName = Table(
      "name",
      "_A",
      "1A",
      "_a",
      "1a",
      "A/",
      "aÀ",
      "a1",
      "a(",
      "a)",
      "a$",
      "a£",
      "A\\"
    )

    val invalidMsg = "Invalid name"

    behave like regexWithValidAndInvalidExamples(name, validName, invalidName, invalidMsg, regexName)
  }

  "emailAddressRestrictive" must {

    val validEmail = Table(
      "ema.il@cd.com",
      "a@email.com",
      "1.2.3@4.5.6"
    )

    val invalidEmail = Table(
      "email@.com",
      "32..423423432423",
      "a@bc",
      "@@@@@@",
      ".df@com",
      "123 2@s.com",
      "xyz;a@value",
      "AÀ@value.com"
    )

    val invalidMsg = "contactDetails.error.email.valid"

    behave like regexWithValidAndInvalidExamples(emailAddressRestrictive, validEmail, invalidEmail, invalidMsg, regexEmailRestrictive)
  }


  "phoneNumber" must {

    val validNumber = Table(
      "phoneNumber",
      "1",
      "99999999999999999999999",
      "123456"
    )

    val invalidNumber = Table(
      "phoneNumber",
      "324234.23432423",
      "123@23",
      "@@@@@@"
    )

    val invalidMsg = "Invalid test"

    behave like regexWithValidAndInvalidExamples(phoneNumber, validNumber, invalidNumber, invalidMsg, regexPhoneNumber)
  }


  "payeEmployerReferenceNumber" must {

    val validPaye = Table(
      "paye",
      "123A",
      "123abcdefghijklm",
      "123ABCDEFGHIJKLM",
      "0001234567890123",
      "121AB45CD67QWERT"
    )

    val invalidPaye = Table(
      "paye",
      "123",
      "123abcdefghijklmN",
      "12abcdefghijklm",
      "123***?."
    )

    val invalidMsg = "payeEmployerReferenceNumber.error.invalid"

    behave like regexWithValidAndInvalidExamples(payeEmployerReferenceNumber, validPaye, invalidPaye, invalidMsg, regexPaye)
  }


  "safeText" must {

    val validText = Table(
      "text",
      "some valid text À ÿ",
      "!$%&*()[]@@'~#;:,./?^",
      "s\\as"
    )

    val invalidText = Table(
      "text",
      "{invalid text}",
      "<invalid>"
    )

    val invalidMsg = "Invalid text"

    behave like regexWithValidAndInvalidExamples(safeText, validText, invalidText, invalidMsg, regexSafeText)

  }

  "policyNumber" must {

    val validText = Table(
      "text",
      "some valid text À ÿ",
      "!$%&*()[]@@'~#;:,./?^",
      "s\\as"
    )

    val invalidText = Table(
      "text",
      "{invalid text}",
      "<invalid>"
    )

    val invalidMsg = "Invalid text"

    behave like regexWithValidAndInvalidExamples(policyNumber, validText, invalidText, invalidMsg, regexPolicyNumber)

  }

  "addressLine" must {

    val validAddress = Table(
      "address",
      "1 Main St.",
      "Apt/12"
    )

    val invalidAddress = Table(
      "address",
      "Apt [12]",
      "Apt\\12",
      "Street À"
    )

    val invalidMsg = "Invalid address"

    behave like regexWithValidAndInvalidExamples(addressLine, validAddress, invalidAddress, invalidMsg, regexAddressLine)

  }

  "postCode" must {

    val validPostCode = Table(
      "postCode",
      "A12 1AB",
      "AB12 1AB",
      "AB1A 1AB",
      "AB121AB",
      "A1 1AA"
    )

    val invalidPostCode = Table(
      "postCode",
      "0B12 1AB",
      "A012 1AB",
      "ABC2 1AB",
      "AB12 AAB",
      "AB12 11B",
      "AB12 1A1",
      "aB12 1AB",
      "Ab12 1AB",
      "AB1a 1AB",
      "A 1AA",
      "A1 1 AA",
      "AB121A A"
    )

    val invalidMsg = "Invalid post code"

    behave like regexWithValidAndInvalidExamples(postCode, validPostCode, invalidPostCode, invalidMsg, regexPostcode)
  }

  "adviserName" must {

    val validText = Table(
      "text",
      "abcd",
      "ABCD",
      "ab'cd",
      "ab-cd",
      "ab cd",
      "1234"
    )

    val invalidText = Table(
      "test<name",
      "1234>",
      ""
    )

    val invalidMsg = "Invalid text"

    behave like regexWithValidAndInvalidExamples(adviserName, validText, invalidText, invalidMsg, adviserNameRegex)
  }

  "notBeforeYear" must {

    "return valid if date is greater than constraint" in {

      notBeforeYear("error.invalid", 1900).apply(LocalDate.now()) mustBe Valid
    }

    "return invalid if date is less than constraint" in {

      notBeforeYear("error.invalid", 1900).apply(LocalDate.parse("1889-06-15")) mustBe Invalid("error.invalid")
    }

  }

}