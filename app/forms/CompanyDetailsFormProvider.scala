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

import forms.mappings._
import models.CompanyDetails
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class CompanyDetailsFormProvider @Inject() extends Mappings with Transforms {

  val companyNameLength: Int = 160

  def apply(): Form[CompanyDetails] = Form(
    mapping(
      "companyName" -> text("messages__error__company_name")
        .verifying(
          firstError(
            maxLength(
              companyNameLength,
              "messages__error__company_name_length"
            ),
            tightTextWithNumber("messages__error__company_name_invalid")
          )
        )
    )(CompanyDetails.applyDelete)(CompanyDetails.unapplyDelete)
  )
}
