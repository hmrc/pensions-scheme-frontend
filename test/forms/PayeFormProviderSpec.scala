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
import forms.behaviours.PayeBehaviours
import models.ReferenceValue
import play.api.data.Form
import viewmodels.Message

class PayeFormProviderSpec extends PayeBehaviours with SpecBase{

  private val requiredPayeKey = "messages__enterPAYE__error_required"
  private val invalidPayeKey = Message("messages__enterPAYE__error_invalid", "test company").resolve

  "A form with Paye" should {

    val testForm = new PayeFormProvider().apply("test company")

    behave like formWithPayeVariations(
      testForm: Form[ReferenceValue],
      requiredPayeKey: String,
      invalidPayeKey: String
    )

  }
}
