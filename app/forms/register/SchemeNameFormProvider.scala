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

import forms.mappings.{Constraints, Mappings}
import play.api.data.Form

import javax.inject.Inject

class SchemeNameFormProvider @Inject() extends Mappings with Constraints {
  val schemeNameMaxLength = 160

  def apply(): Form[String] = Form(
    "schemeName" -> text("messages__error__scheme_name").
      verifying(firstError(
        maxLength(schemeNameMaxLength, "messages__error__scheme_name_length"),
        tightTextWithNumber("messages__error__scheme_name_invalid")))
  )
}
