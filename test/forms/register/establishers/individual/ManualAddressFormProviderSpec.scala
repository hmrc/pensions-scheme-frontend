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
import models.addresslookup.Address
import models.{Field, Required}
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.FormError

class ManualAddressFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "addressLine1" -> "address line 1",
    "addressLine2" -> "address line 2",
    "addressLine3" -> "address line 3",
    "addressLine4" -> "address line 4",
    "postCode.postCode" -> "AB1 1AP",
    "country" -> "GB"
  )

  val postCodeRegex = "^(?i)[A-Z]{1,2}[0-9][0-9A-Z]?[ ]?[0-9][A-Z]{2}"
  val form = new ManualAddressFormProvider()()

  "ManualAddress form" must {
    behave like questionForm(Address("address line 1", "address line 2",
      Some("address line 3"), Some("address line 4"), Some("AB1 1AP"), "GB"))

    behave like formWithMandatoryTextFields(
      Field("addressLine1", Required -> "messages__error__addr1"),
      Field("addressLine2", Required -> "messages__error__addr2"),
      Field("country", Required -> "messages__error__scheme_country"
      )
    )

    "successfully bind when country is not UK and postcode is any postcode" in {
      val data = validData + ("postCode.postCode" -> "zxadsafd", "country" -> "AF")
      val result = form.bind(data)
      result.get shouldEqual Address("address line 1", "address line 2",
        Some("address line 3"), Some("address line 4"), Some("zxadsafd"), "AF")
    }

    "successfully bind when country is UK and postcode is a valid postcode" in {
      val data = validData + ("postCode.postCode" -> "AB1 1AB", "country" -> "GB")
      val result = form.bind(data)
      result.get shouldEqual Address("address line 1", "address line 2",
        Some("address line 3"), Some("address line 4"), Some("AB1 1AB"), "GB")
    }

    "fail to bind when address line 1 exceeds max length 35" in {
      val addressLine1 = RandomStringUtils.randomAlphabetic(36)
      val data = validData + ("addressLine1" -> addressLine1)

      val expectedError: Seq[FormError] = error("addressLine1", "messages__error__addr1_length", 35)
      checkForError(form, data, expectedError)
    }

    "fail to bind when address line 2 exceeds max length 35" in {
      val addressLine2 = RandomStringUtils.randomAlphabetic(36)
      val data = validData + ("addressLine2" -> addressLine2)

      val expectedError: Seq[FormError] = error("addressLine2", "messages__error__addr2_length", 35)
      checkForError(form, data, expectedError)
    }

    "fail to bind when address line 3 exceeds max length 35" in {
      val addressLine3 = RandomStringUtils.randomAlphabetic(36)
      val data = validData + ("addressLine3" -> addressLine3)

      val expectedError: Seq[FormError] = error("addressLine3", "messages__error__addr3_length", 35)
      checkForError(form, data, expectedError)
    }

    "fail to bind when address line 4 exceeds max length 35" in {
      val addressLine4 = RandomStringUtils.randomAlphabetic(36)
      val data = validData + ("addressLine4" -> addressLine4)

      val expectedError: Seq[FormError] = error("addressLine4", "messages__error__addr4_length", 35)
      checkForError(form, data, expectedError)
    }

    "fail to bind when postcode is missing for country UK" in {
      val validData: Map[String, String] = Map(
        "addressLine1" -> "address line 1",
        "addressLine2" -> "address line 2",
        "addressLine3" -> "address line 3",
        "addressLine4" -> "address line 4",
        "country" -> "GB"
      )
      val expectedError: Seq[FormError] = error("postCode.postCode", "messages__error__postcode")
      checkForError(form, validData, expectedError)
    }
  }
}
