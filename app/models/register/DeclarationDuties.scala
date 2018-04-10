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

sealed trait DeclarationDuties

object DeclarationDuties {

  case object WorkingKnowledge extends WithName("working_knowledge") with DeclarationDuties
  case object AppointedAdvisor extends WithName("appointed_advisor") with DeclarationDuties

  val values: Seq[DeclarationDuties] = Seq(
    WorkingKnowledge, AppointedAdvisor
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString, s"messages__declarationDuties__${value.toString}")
  }

  implicit val enumerable: Enumerable[DeclarationDuties] =
    Enumerable(values.map(v => v.toString -> v): _*)
}