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

package forms.register

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import models.person.PersonDetails
import play.api.data.Form
import play.api.data.Forms._

class PersonDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[PersonDetails] = Form(
    mapping(
      "firstName" ->
        text(errorKey = "messages__error__first_name")
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.firstNameLength,
                errorKey = "messages__error__first_name_length"
              ),
              name(errorKey = "messages__error__first_name_invalid")
            )
          ),
      "middleName" ->
        optionalText()
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.middleNameLength,
                errorKey = "messages__error__middle_name_length"
              ),
              name(errorKey = "messages__error__middle_name_invalid")
            )
          ),
      "lastName" ->
        text(errorKey = "messages__error__last_name")
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.lastNameLength,
                errorKey = "messages__error__last_name_length"
              ),
              name(errorKey = "messages__error__last_name_invalid")
            )
          ),
      "date" -> dateMapping("messages__error__date", "error.invalid_date")
        .verifying(futureDate("messages__error__date_future"))
    )(PersonDetails.applyDelete)(PersonDetails.unapplyDelete)
  )
}

object PersonDetailsFormProvider {
  val firstNameLength: Int = 35
  val middleNameLength: Int = 35
  val lastNameLength: Int = 35
}