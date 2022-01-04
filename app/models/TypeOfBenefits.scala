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

import utils.{Enumerable, InputOption, WithName}

sealed trait TypeOfBenefits

object TypeOfBenefits {
  val values: Seq[TypeOfBenefits] = Seq(
    MoneyPurchase, Defined, MoneyPurchaseDefinedMix
  )
  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"messages__type_of_benefits__${value.toString}")
  }

  case object MoneyPurchase extends WithName("opt1") with TypeOfBenefits

  case object Defined extends WithName("opt2") with TypeOfBenefits

  case object MoneyPurchaseDefinedMix extends WithName("opt3") with TypeOfBenefits

  implicit val enumerable: Enumerable[TypeOfBenefits] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
