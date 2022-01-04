/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.Inject
import forms.mappings._
import play.api.data.Form
import play.api.i18n.Messages

class NoCompanyNumberFormProvider @Inject() extends Mappings with Transforms {

  val maxLength = 160

  def apply(name: String)(implicit messages: Messages): Form[String] = Form(
    "reason" -> text(Messages("messages__error__no_company_number", name)).
      verifying(firstError(
        maxLength(maxLength, "messages__error__no_company_number_maxlength"),
        safeText("messages__error__no_company_number_invalid")))
  )
}
