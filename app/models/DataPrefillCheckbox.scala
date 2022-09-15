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

package models

import models.prefill.{IndividualDetails => DataPrefillIndividualDetails}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem

object DataPrefillCheckbox {

  def checkboxes(values: Seq[DataPrefillIndividualDetails])(implicit messages: Messages): Seq[CheckboxItem] = {
    val noneValue = "-1"
    val rr = values.map{ x =>
      CheckboxItem(
        content = Text(x.fullName),
        value = x.index.toString
      )
    }
    rr :+ CheckboxItem(
      content = Text(messages("messages__prefill__label__none")),
      value = noneValue
    )
  }
}
