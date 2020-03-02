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

package forms

import base.SpecBase
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError
import viewmodels.Message

class OccupationalPensionSchemeFormProviderSpec extends BooleanFieldBehaviours with SpecBase {

  private val schemeName = "Scheme name"
  private val requiredKey = Message("messages__occupational_pension_scheme__error_required", schemeName)
  private val invalidKey = "error.boolean"

  val form = new OccupationalPensionSchemeFormProvider()(schemeName)
  private val fieldName = "value"

  "OccupationalPensionScheme Form Provider" must {

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
