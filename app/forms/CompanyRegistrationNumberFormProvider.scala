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

import forms.mappings.CrnMapping
import javax.inject.Inject
import models.ReferenceValue
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import viewmodels.Message

class CompanyRegistrationNumberFormProvider @Inject() extends CrnMapping {

  def apply(name: String)(implicit messages: Messages): Form[ReferenceValue] =
    Form(
      mapping(
        "companyRegistrationNumber" -> crnMapping(
          crnLengthKey = Message("messages__error__no_crn_length", name),
          invalidCRNKey = Message("messages__error__crn_invalid_with_company_name", name).resolve
        )
      )(ReferenceValue.applyEditable)(ReferenceValue.unapplyEditable)
    )
}
