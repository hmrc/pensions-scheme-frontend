/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.behaviours.StringFieldBehaviours
import play.api.data.{Form, FormError}
import viewmodels.Message

class ReasonFormProviderSpec extends StringFieldBehaviours with SpecBase{

  private val reasonMaxLength = 160
  private val reasonLengthKey = "messages__reason__error_maxLength"
  private val requiredReasonKey = Message("messages__reason__error_utrRequired", "test company").resolve
  private val invalidReasonKey = "messages__reason__error_invalid"
  private def formError(errorKey: String) = FormError("reason", errorKey)
  private val regexSafeText = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]|~£€¥\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,160}$"""

  "A form with a Reason" should {
    val testForm = new ReasonFormProvider().apply("messages__reason__error_utrRequired", "test company")

    behave like fieldWithMaxLength(testForm: Form[String], "reason", reasonMaxLength, FormError("reason", reasonLengthKey, List(160)))
    behave like fieldWithRegex(testForm: Form[String], "reason", "abc^¬66", FormError("reason", invalidReasonKey, List(regexSafeText)))
    behave like mandatoryField(testForm: Form[String], "reason", formError(requiredReasonKey))

  }
}
