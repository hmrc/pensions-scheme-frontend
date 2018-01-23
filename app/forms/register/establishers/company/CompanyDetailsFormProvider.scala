/*
 * Copyright 2018 HM Revenue & Customs
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

package forms.register.establishers.company

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import models.CompanyDetails

class CompanyDetailsFormProvider @Inject() extends Mappings {

  val companyNameMaxLength = 160
  val payeNumberMaxLength = 13

   def apply(): Form[CompanyDetails] = Form(
     mapping(
      "companyName" -> text("messages__error__company_name").verifying(
        maxLength(companyNameMaxLength, "messages__error__company_name_length")),
      "vatNumber" -> optional(
        vatMapping("messages__error__vat_invalid", "messages__error__vat_length")),
      "payeNumber" -> optional(Forms.text.verifying(
        maxLength(payeNumberMaxLength, "messages__error__paye_length")))
    )(CompanyDetails.apply)(CompanyDetails.unapply)
   )
 }
