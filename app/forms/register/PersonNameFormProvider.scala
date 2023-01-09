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

package forms.register

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import models.person.PersonName
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

class PersonNameFormProvider @Inject() extends Mappings with Transforms {

  def apply(token: String)(implicit messages: Messages): Form[PersonName] =
    Form(
      mapping(
        "firstName" ->
          text(errorKey = messages("messages__error__first_name", messages(token))).verifying(
            firstError(
              maxLength(PersonNameFormProvider.firstNameLength, errorKey = "messages__error__first_name_length"),
              name(errorKey = "messages__error__first_name_invalid")
            )
          ),
        "lastName" ->
          text(errorKey = messages("messages__error__last_name", messages(token))).verifying(
            firstError(
              maxLength(PersonNameFormProvider.lastNameLength, errorKey = "messages__error__last_name_length"),
              name(errorKey = "messages__error__last_name_invalid")
            )
          )

      )(PersonName.applyDelete)(PersonName.unapplyDelete)
    )
}

object PersonNameFormProvider {
  val firstNameLength: Int = 35
  val lastNameLength: Int = 35
}
