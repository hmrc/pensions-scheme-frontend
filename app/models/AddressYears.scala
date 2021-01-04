/*
 * Copyright 2021 HM Revenue & Customs
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

import utils.{Enumerable, InputOption, WithName}

sealed trait AddressYears

object AddressYears extends Enumerable.Implicits {
  val values: Seq[AddressYears] = Seq(
    OverAYear,
    UnderAYear
  )
  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"messages__common__${value.toString}")
  }

  case object UnderAYear extends WithName("under_a_year") with AddressYears

  case object OverAYear extends WithName("over_a_year") with AddressYears

  implicit val enumerable: Enumerable[AddressYears] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
