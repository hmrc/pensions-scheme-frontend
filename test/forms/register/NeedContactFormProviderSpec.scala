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

package forms.register

import forms.behaviours.EmailBehaviours
import play.api.data.{Form, Mapping}

class NeedContactFormProviderSpec extends EmailBehaviours {

  val fieldName = "email"
  val keyEmailRequired = "messages__error__email"
  val keyEmailLength = "messages__error__email_length"
  val keyEmailInvalid = "messages__error__email_invalid"

  val mapping: Mapping[String] = emailMapping(keyEmailRequired, keyEmailLength, keyEmailInvalid)
  val form: Form[String] = Form(fieldName -> mapping)

  behave like formWithEmailField(
    form,
    fieldName,
    keyEmailRequired,
    keyEmailLength,
    keyEmailInvalid
  )
}
