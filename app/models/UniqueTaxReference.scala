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

sealed trait UniqueTaxReference

object UniqueTaxReference {

  case class Yes(utr: String) extends UniqueTaxReference
  case class No(reason: String) extends UniqueTaxReference

  implicit val reads: Reads[UniqueTaxReference] = {

    (JsPath \ "hasUtr").read[String].flatMap {

      case hasUtr if hasUtr == "yes" =>
        (JsPath \ "utr").read[String]
          .map[UniqueTaxReference](Yes.apply)
          .orElse(Reads[UniqueTaxReference](_ => JsError("Utr Value expected")))

      case hasUtr if hasUtr == "no" =>
        (JsPath \ "reason").read[String]
          .map[UniqueTaxReference](No.apply)
          .orElse(Reads[UniqueTaxReference](_ => JsError("Reason expected")))

      case _ => Reads(_ => JsError("Invalid selection"))
    }
  }

  implicit lazy val writes = new Writes[UniqueTaxReference] {
    def writes(o: UniqueTaxReference) = {
      o match {
        case UniqueTaxReference.Yes(utr) =>
          Json.obj("hasUtr" -> "yes", "utr" -> utr)
        case UniqueTaxReference.No(reason) =>
          Json.obj("hasUtr" -> "no", "reason" -> reason)
      }
    }
  }
}



