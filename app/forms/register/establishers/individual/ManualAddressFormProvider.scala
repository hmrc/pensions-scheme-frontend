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

package forms.register.establishers.individual

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import models.register.establishers.individual.ManualAddress

class ManualAddressFormProvider @Inject() extends Mappings {

  val addressLineMaxLength = 160

   def apply(): Form[ManualAddress] = Form(
     mapping(
      "addressLine1" -> text("messages__error__addr1"),
      "addressLine2" -> text("messages__error__addr2"),
      "addressLine3" -> optional(Forms.text.verifying(
        maxLength(addressLineMaxLength, ""))),
      "addressLine4" -> optional(Forms.text.verifying(
        maxLength(addressLineMaxLength, ""))),
      "postalCode" -> optional(Forms.text.verifying(
        maxLength(addressLineMaxLength, ""))),
      "country" -> text("messages__error__country")
    )(ManualAddress.apply)(ManualAddress.unapply)
   )
 }
