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
import utils.InputOption

object DataPrefillRadioOptions {
  def apply(values: Seq[IndividualDetails])(implicit messages: Messages): Seq[InputOption] = {
    values
      .map { individualDetails =>
        InputOption(individualDetails.index.toString, individualDetails.fullName)
      } :+
      InputOption("-1", messages("messages__prefill__label__none"))
  }
}
