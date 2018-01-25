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
import models.addresslookup.{Address, Country}

class AddressResultsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "value" -> "address line 1, address line 2, test town, test county, test post code, United Kingdom"
  )
  val address = Address(List("address line 1", "address line 2"), Some("test town"),
    Some("test county"), "test post code", Country("United Kingdom"))

  val form = new AddressResultsFormProvider()()

  "AddressResults form" must {

    behave like questionForm[Address](address)

    "fail to bind when value is omitted" in {
      val expectedError = error("value", "messages__error__select_address")
      checkForError(form, emptyForm, expectedError)
    }
  }
}
