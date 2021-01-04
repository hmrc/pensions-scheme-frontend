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

package forms.register.adviser

import forms.behaviours.PhoneNumberBehaviours
import forms.mappings.Constraints

class AdviserPhoneFormProviderSpec extends PhoneNumberBehaviours with Constraints {

  val form = new AdviserPhoneFormProvider()()

  ".phone" must {

    val fieldName = "phone"

    val requiredKey = "messages__phone__blank"
    val maxLengthKey = "messages__phone__length"
    val invalidKey = "messages__phone__invalid"

    behave like formWithPhoneNumberField(form, fieldName, requiredKey, maxLengthKey, invalidKey)

    behave like formWithTransform[String](
      form,
      Map(fieldName -> " 0000 "),
      "0000"
    )
  }
}
