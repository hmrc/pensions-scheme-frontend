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

package forms

import base.SpecBase
import forms.behaviours.CrnBehaviour
import models.ReferenceValue
import play.api.data.Form
import viewmodels.Message

class CompanyRegistrationNumberFormProviderSpec extends CrnBehaviour with FormSpec with SpecBase{

  private val lengthKey = Message("messages__error__no_crn_length", "company name").resolve
  private val requiredKey = "messages__error__company_number"
  private val invalidKey = Message("messages__error__crn_invalid_with_company_name", "company name").resolve

  "A form with a CRNNumber" should {
    val testForm = new CompanyRegistrationNumberFormProvider().apply("company name")

    behave like formWithCrnVariations(testForm: Form[ReferenceValue],
      lengthKey: String,
      requiredKey: String,
      invalidKey: String
    )
  }
}
