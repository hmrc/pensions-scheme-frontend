/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.behaviours.EmailBehaviours
import play.api.data.{Form, Mapping}

class EmailMappingSpec extends EmailBehaviours {

  "Email mapping with all errors" should {
    val fieldName = "email"
    val keyEmailRequired = "messages__error__adviser__email__address__required"
    val keyEmailLength = "messages__error__adviser__email__address__length"
    val keyEmailInvalid = "messages__error__adviser__email__address__invalid"

    val mapping: Mapping[String] = emailMappingWithAllErrors(keyEmailRequired, keyEmailLength, keyEmailInvalid)
    val form: Form[String] = Form(fieldName -> mapping)

    behave like formWithEmailFieldForAllErrors(
      form,
      fieldName,
      keyEmailRequired,
      keyEmailLength,
      keyEmailInvalid
    )
  }

}
