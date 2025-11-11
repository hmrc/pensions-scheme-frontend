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

package models

import models.prefill.IndividualDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ExclusiveCheckbox, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxBehaviour, CheckboxItem}

object DataPrefillCheckboxOptions {

  def apply(values: Seq[IndividualDetails])(implicit messages: Messages): Seq[CheckboxItem] = {
    values.zipWithIndex.map { case (individualDetails, index) =>
      CheckboxItem(
        content = Text(individualDetails.fullName),
        value   = index.toString
      )
    } :++ Seq(
      CheckboxItem(
        divider = Some("or")
      ),
      CheckboxItem(
        content   = Text(messages("messages__prefill__label__none")),
        value     = "-1",
        behaviour = Some(ExclusiveCheckbox)
      )
    )
  }
}
