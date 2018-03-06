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

package forms.register.establishers.individual

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.register.establishers.individual.ContactDetails
import org.apache.commons.lang3.RandomStringUtils

class ContactDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "emailAddress" -> "test@test.com",
    "phoneNumber" -> "123456789"
  )
  val emailRegex = "^[^@<>]+@[^@<>]+$"
  val regexPhoneNumber = "\\d*"
  val form = new ContactDetailsFormProvider()()

  "ContactDetails form" must {
    behave like questionForm(ContactDetails("test@test.com", "123456789"))

    behave like formWithMandatoryTextFields(
      Field("emailAddress", Required -> "messages__error__email"),
      Field("phoneNumber", Required -> "messages__error__phone")
    )

    Seq("@test.com", "<>@ghghg", "fhgfhgfggf", "test@<>.com").foreach { email =>
      s"fail to bind when the email $email is invalid" in {
        val data = validData + ("emailAddress" -> email)

        val expectedError = error("emailAddress", "messages__error__email_invalid", emailRegex)
        checkForError(form, data, expectedError)
      }
    }

    "fail to bind when email exceeds max length 132" in {
      val maxlengthEmail = 132
      val testString = s"${RandomStringUtils.random(50)}@${RandomStringUtils.random(82)}"
      val data = validData + ("emailAddress" -> testString)

      val expectedError = error("emailAddress", "messages__error__email_length", maxlengthEmail)
      checkForError(form, data, expectedError)
    }

    Seq("zfsadfdsf", "<>13213cvfdv").foreach { phoneNo =>
      s"fail to bind when the phone number $phoneNo is invalid" in {
        val data = validData + ("phoneNumber" -> phoneNo)

        val expectedError = error("phoneNumber", "messages__error__phone_invalid", regexPhoneNumber)
        checkForError(form, data, expectedError)
      }
    }

    "fail to bind when phoneNumber exceeds max length 24" in {
      val invalidPhoneNumber = "1234567890123456789012345"
      val maxlengthPhone = 24
      val data = validData + ("phoneNumber" -> invalidPhoneNumber)

      val expectedError = error("phoneNumber", "messages__error__phone_length", maxlengthPhone)
      checkForError(form, data, expectedError)
    }
  }

}
