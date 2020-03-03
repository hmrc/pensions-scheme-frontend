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

class HasBeenTradingFormProviderSpec extends BooleanFieldBehaviours with SpecBase {

  private val requiredKey = Message("messages__hasBeenTradingCompany__error__required", "ABC").resolve
  private val invalidKey = "error.boolean"
  private val fieldName = "value"

  private def formProvider(companyName:String) =
    new HasBeenTradingFormProvider()("messages__hasBeenTradingCompany__error__required", companyName)

  "HasBeenTrading Form Provider" must {

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
