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

import config.FrontendAppConfig
import utils.{Enumerable, InputOption, WithName}

sealed trait DeclarationDormant

object DeclarationDormant {

  case object No extends WithName("no") with DeclarationDormant

  case object Yes extends WithName("yes") with DeclarationDormant

  val values: Seq[DeclarationDormant] = Seq(
    No, Yes
  )

  def options(config: FrontendAppConfig): Seq[InputOption] = values.map {
    value =>
      if(config.isHubEnabled)
        InputOption(value.toString, s"messages__is_dormant__${value.toString}")
      else
        InputOption(value.toString, s"messages__declarationDormant__${value.toString}")
  }

  implicit val enumerable: Enumerable[DeclarationDormant] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
