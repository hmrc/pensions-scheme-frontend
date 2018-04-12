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

package models.register

import utils.{Enumerable, InputOption, WithName}

sealed trait DeclarationDormant

object DeclarationDormant {

  case object Option1 extends WithName("option1") with DeclarationDormant
  case object Option2 extends WithName("option2") with DeclarationDormant

  val values: Seq[DeclarationDormant] = Seq(
    Option1, Option2
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"messages__declarationDormant__${value.toString}")
  }

  implicit val enumerable: Enumerable[DeclarationDormant] =
    Enumerable(values.map(v => v.toString -> v): _*)
}