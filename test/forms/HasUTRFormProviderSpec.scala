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
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError
import viewmodels.Message

class HasUTRFormProviderSpec extends BooleanFieldBehaviours with SpecBase {

  private val requiredKey = Message("messages__hasCompanyUtr__error__required", "ABC").resolve
  private val invalidKey = "error.boolean"
  private val fieldName = "value"

  def formProvider(companyName:String) = new HasUTRFormProvider()("messages__hasCompanyUtr__error__required", companyName)

  "HasCompanyUtr Form Provider" must {

    behave like booleanField(
      formProvider("ABC"),
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      formProvider("ABC"),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
