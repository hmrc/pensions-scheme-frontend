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
import models.{EstablisherNino, EstablisherNinoYes, SchemeDetails}
import play.api.data.Form
import play.api.data.Forms.mapping

class EstablisherNinoFormProvider @Inject() extends Mappings {
  val ninoMaxLength = 9

  def apply(): Form[EstablisherNino] = Form(mapping(
    "ninoEntry" -> text(
      "messages__error__nino").
      verifying(maxLength(ninoMaxLength, "messages__error__scheme_name_length")),
    "yesNo" -> establisherNinoMapping()
  )(EstablisherNinoYes.apply)(EstablisherNinoYes.unapply))
}
