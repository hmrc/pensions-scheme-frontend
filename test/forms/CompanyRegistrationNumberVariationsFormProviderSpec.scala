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

import com.google.inject.Inject
import forms.behaviours.CrnBehaviour
import play.api.data.Form
import play.api.i18n.Messages

class CompanyRegistrationNumberVariationsFormProviderSpec @Inject() (implicit messages: Messages) extends CrnBehaviour {

  private val lengthKey = "messages__error__no_crn_length"
  private val requiredKey = "messages__error__company_number"
  private val invalidKey = "messages__error__crn_invalid"

  "A form with a CRNNumber" should {
    val testForm = new CompanyRegistrationNumberVariationsFormProvider().apply("company name")

    behave like formWithCrnVariations(testForm: Form[String],
      lengthKey: String,
      requiredKey: String,
      invalidKey: String
    )
  }
}
