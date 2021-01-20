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

import utils.{Enumerable, WithName}
import viewmodels.Checkbox


sealed trait MoneyPurchaseBenefits

object MoneyPurchaseBenefits extends Enumerable.Implicits {

  case object Collective extends WithName("opt1") with MoneyPurchaseBenefits
  case object CashBalance extends WithName("opt2") with MoneyPurchaseBenefits
  case object Other extends WithName("opt3") with MoneyPurchaseBenefits

  def values: Seq[MoneyPurchaseBenefits] = Seq(Collective, CashBalance, Other)

  val options: Seq[Checkbox] = values.map { value => Checkbox("messages__moneyPurchaseBenefits__", value.toString) }

  implicit val enumerable: Enumerable[MoneyPurchaseBenefits] = Enumerable(values.map(v => v.toString -> v): _*)
}