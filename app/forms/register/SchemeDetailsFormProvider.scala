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

package forms.register

import forms.mappings.{Constraints, SchemeTypeMapping}
import javax.inject.Inject
import models.register.SchemeDetails
import play.api.data.Form
import play.api.data.Forms._

class SchemeDetailsFormProvider @Inject() extends SchemeTypeMapping with Constraints {
  val schemeNameMaxLength = 160

  def apply(): Form[SchemeDetails] = Form(mapping(
    "schemeName" -> text(
      "messages__error__scheme_name").
      verifying(firstError(
        maxLength(schemeNameMaxLength, "messages__error__scheme_name_length"),
        safeText("messages__error__scheme_name_invalid"))),
    "schemeType" -> schemeTypeMapping()
  )(SchemeDetails.apply)(SchemeDetails.unapply))
}
