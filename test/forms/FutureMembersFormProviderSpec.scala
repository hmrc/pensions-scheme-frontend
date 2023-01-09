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

package forms

import base.SpecBase
import forms.behaviours.FormBehaviours
import models.{Field, Invalid, Members, Required}
import viewmodels.Message

class FutureMembersFormProviderSpec extends FormBehaviours with SpecBase {

  val schemeName = "Scheme name"
  val validData: Map[String, String] = Map(
    "value" -> Members.options.head.value
  )

  val form = new FutureMembersFormProvider()(schemeName)

  "Current Members form" must {

    behave like questionForm[Members](Members.values.head)

    behave like formWithOptionField(
      Field(
        "value",
        Required -> Message("messages__future_members__error_required", schemeName),
        Invalid -> "error.invalid"),
      Members.options.map(_.value): _*)
  }
}
