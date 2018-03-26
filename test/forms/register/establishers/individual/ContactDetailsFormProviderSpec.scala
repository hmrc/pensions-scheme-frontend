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

import forms.ContactDetailsFormProvider
import forms.behaviours.{EmailBehaviours, PhoneNumberBehaviours, StringFieldBehaviours}
import forms.mappings.Constraints

class ContactDetailsFormProviderSpec extends StringFieldBehaviours with EmailBehaviours with PhoneNumberBehaviours with Constraints {

  val form = new ContactDetailsFormProvider()()

  ".email" must {

    val fieldName = "emailAddress"
    val keyEmailRequired = "messages__error__email"
    val keyEmailLength = "messages__error__email_length"
    val keyEmailInvalid = "messages__error__email_invalid"

    behave like formWithEmailField(
      form,
      fieldName,
      keyEmailRequired,
      keyEmailLength,
      keyEmailInvalid
    )

  }

  ".phone" must {
    val fieldName = "phoneNumber"
    val keyPhoneNumberRequired = "messages__error__phone"
    val keyPhoneNumberLength = "messages__error__phone_length"
    val keyPhoneNumberInvalid = "messages__error__phone_invalid"

    behave like formWithPhoneNumberField(
      form,
      fieldName,
      keyPhoneNumberRequired,
      keyPhoneNumberLength,
      keyPhoneNumberInvalid
    )
  }
}
