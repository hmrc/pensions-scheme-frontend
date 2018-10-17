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

package models.details.view

import viewmodels.AnswerRow
import scala.language.implicitConversions

trait TransformedElement[I] {

  def transformRows(data: I): Seq[AnswerRow]

  def transformRow(label: String,
                   answer: Seq[String],
                   answerIsMessageKey: Boolean=false,
                   changeUrl: Option[String] = None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl)
  }
}
