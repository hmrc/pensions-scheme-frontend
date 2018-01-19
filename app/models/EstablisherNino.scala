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
    InputOption("true", "site.yes", Some("establisherNino_nino-form")),
    InputOption("false", "site.no", Some("establisherNino_reason-form"))
  )

  implicit val reads: Reads[EstablisherNino] = {

    (JsPath \ "hasNino").read[Boolean].flatMap {

      case true =>
        (JsPath \ "nino").read[String]
          .map[EstablisherNino](Yes.apply)
          .orElse(Reads[EstablisherNino](_ => JsError("NINO Value expected")))

      case false =>
        (JsPath \ "reason").read[String]
          .map[EstablisherNino](No.apply)
          .orElse(Reads[EstablisherNino](_ => JsError("Reason expected")))
    }
  }

  implicit lazy val writes = new Writes[EstablisherNino] {
    def writes(o: EstablisherNino) = {
      o match {
        case EstablisherNino.Yes(nino) =>
          Json.obj("hasNino" -> true, "nino" -> nino)
        case EstablisherNino.No(reason) =>
          Json.obj("hasNino" -> false, "reason" -> reason)
      }
    }
  }
}