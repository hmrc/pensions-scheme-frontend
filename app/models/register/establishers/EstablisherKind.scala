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

package models.register.establishers

import play.api.mvc.JavascriptLiteral
import utils.{Enumerable, InputOption, WithName}

sealed trait EstablisherKind

object EstablisherKind {
  val values: Seq[EstablisherKind] = Seq(
    Company, Individual, Partnership
  )
  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"messages__establishers__add__opt__${value.toString}")
  }

  case object Company extends WithName("company") with EstablisherKind

  case object Individual extends WithName("individual") with EstablisherKind

  case object Partnership extends WithName("partnership") with EstablisherKind

  implicit val enumerable: Enumerable[EstablisherKind] =
    Enumerable(values.map(v => v.toString -> v)*)

  //noinspection ConvertExpressionToSAM
  implicit val jsLiteral: JavascriptLiteral[EstablisherKind] = new JavascriptLiteral[EstablisherKind] {
    override def to(value: EstablisherKind): String = value match {
      case Company => "Company"
      case Individual => "Individual"
      case Partnership => "Partnership"
    }
  }
}
