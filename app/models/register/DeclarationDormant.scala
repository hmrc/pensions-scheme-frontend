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

package models.register

import utils.{Enumerable, InputOption, WithName}

sealed trait DeclarationDormant

object DeclarationDormant extends Enumerable.Implicits {
  val values: Seq[DeclarationDormant] = Seq(
    Yes, No
  )

  def options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"messages__is_dormant__${value.toString}")
  }

  case object No extends WithName("no") with DeclarationDormant

  case object Yes extends WithName("yes") with DeclarationDormant

  implicit val enumerable: Enumerable[DeclarationDormant] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
