/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{Field, Invalid, Required, TypeOfBenefits}
import viewmodels.Message

class TypeOfBenefitsFormProviderSpec extends FormBehaviours with SpecBase {

  private val schemeName = "Scheme name"
  val validData: Map[String, String] = Map(
    "value" -> TypeOfBenefits.options.head.value
  )

  val form = new TypeOfBenefitsFormProvider()(schemeName)

  "Benefits form" must {

    behave like questionForm[TypeOfBenefits](TypeOfBenefits.values.head)

    behave like formWithOptionField(
      Field(
        "value",
        Required -> Message("messages__type_of_benefits__error_required", schemeName),
        Invalid -> "error.invalid"),
      TypeOfBenefits.options.map(_.value)*)
  }
}
