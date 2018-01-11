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

package models

import models.SchemeType.{Other, mappings}
import play.api.libs.json.{JsError, JsPath, JsSuccess, Reads}
import utils.{InputOption, WithName}

sealed trait EstablisherNino

object EstablisherNino {

  case class Yes(establisherNino: String) extends WithName("yes") with EstablisherNino
  case class No(establisherNino: String) extends WithName("no") with EstablisherNino

  val yes = "yes"
  val no = "no"

  def options: Seq[InputOption] = Seq(
    InputOption(yes, s"messages__establisherNino__$yes", Some("establisherNino__yes-form")),
    InputOption(no, s"messages__establisherNino__$no", Some("establisherNino__no-form"))
  )

  implicit val reads: Reads[EstablisherNino] = {

    (JsPath \ "name").read[String].flatMap {

      case establisherNino if establisherNino == yes =>
        (JsPath \ "establisherNino").read[String]
          .map[EstablisherNino](Yes.apply)
          .orElse(Reads[EstablisherNino](_ => JsError("Other Value expected")))

      case establisherNino if establisherNino == no =>
        (JsPath \ "establisherNino").read[String]
          .map[EstablisherNino](No.apply)
          .orElse(Reads[EstablisherNino](_ => JsError("Other Value expected")))

      case establisherNino if mappings.keySet.contains(establisherNino) =>
        Reads(_ => JsSuccess(mappings.apply(establisherNino)))

      case _ => Reads(_ => JsError("Invalid Scheme Type"))
    }
  }
}