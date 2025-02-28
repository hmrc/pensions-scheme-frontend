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

package forms.register

import forms.mappings.{Mappings, Transforms}
import models.PartnershipDetails
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class PartnershipDetailsFormProvider @Inject() extends Mappings with Transforms {

  val partnerNameLength: Int = 160

  def apply(): Form[PartnershipDetails] = Form(
    mapping(
      "partnershipName" -> text(errorKey = "messages__partnershipDetails__error__required")
        .verifying(
          firstError(
            maxLength(
              partnerNameLength,
              errorKey = "messages__partnershipDetails__error_too_long"
            ),
            safeText(errorKey = "messages__partnershipDetails__invalid")
          )
        )
    )(PartnershipDetails.applyDelete)(PartnershipDetails.unapplyDelete)
  )
}
