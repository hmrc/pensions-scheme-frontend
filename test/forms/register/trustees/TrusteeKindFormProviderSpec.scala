/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.register.trustees

import forms.behaviours.FormBehaviours
import models.register.trustees.TrusteeKind
import models.{Field, Invalid, Required}

class TrusteeKindFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "value" -> TrusteeKind.options.head.value
  )

  val form = new TrusteeKindFormProvider()()

  "TrusteeKind form" must {

    behave like questionForm[TrusteeKind](TrusteeKind.values.head)

    behave like formWithOptionField(
      Field(
        "value",
        Required -> "messages__trusteeKind__error_required",
        Invalid -> "error.invalid"),
      TrusteeKind.options.map(_.value): _*)
  }
}
