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

import play.api.libs.json._
import utils.InputOption

sealed trait EstablisherNino

object EstablisherNino {

  case class Yes(nino: String) extends EstablisherNino
  case class No(reason: String) extends EstablisherNino

  def options: Seq[InputOption] = Seq(
    InputOption("yes", "site.yes", Some("establisherNino_nino-form")),
    InputOption("no", "site.no", Some("establisherNino_reason-form"))
  )

  implicit val reads: Reads[EstablisherNino] = {

    (JsPath \ "hasNino").read[String].flatMap {

      case hasNino if hasNino == "yes" =>
        (JsPath \ "nino").read[String]
          .map[EstablisherNino](Yes.apply)
          .orElse(Reads[EstablisherNino](_ => JsError("NINO Value expected")))

      case hasNino if hasNino == "no" =>
        (JsPath \ "reason").read[String]
          .map[EstablisherNino](No.apply)
          .orElse(Reads[EstablisherNino](_ => JsError("Reason expected")))

      case _ => Reads(_ => JsError("Invalid selection"))
    }
  }

  implicit lazy val writes = new Writes[EstablisherNino] {
    def writes(o: EstablisherNino) = {
      o match {
        case EstablisherNino.Yes(nino) =>
          Json.obj("hasNino" -> "yes", "nino" -> nino)
        case EstablisherNino.No(reason) =>
          Json.obj("hasNino" -> "no", "reason" -> reason)
      }
    }
  }
}