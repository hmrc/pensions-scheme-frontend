/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.mappings.EmailMapping
import javax.inject.Inject
import models.UserResearchDetails
import play.api.data.Form
import play.api.data.Forms._

class UserResearchDetailsFormProvider @Inject() extends EmailMapping {


  def apply(): Form[UserResearchDetails] = Form(
    mapping(
      "name" -> text("messages__userResearchDetails__error_name_required")
        .verifying(
          firstError(
            maxLength(
              UserResearchDetailsFormProvider.nameLength,
              "messages__userResearchDetails__error_name_length"
            ),
            userResearchName("messages__userResearchDetails__error_name_invalid")
          )
        ),
      "email" -> emailMapping(
        "messages__error__email",
        "messages__error__email_length",
        "messages__error__email_invalid"
      )
    )(UserResearchDetails.apply)(UserResearchDetails.unapply)
  )
}

object UserResearchDetailsFormProvider {
  val nameLength: Int = 160
}

