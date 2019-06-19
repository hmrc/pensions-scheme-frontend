/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Reference(value: String, isEditable: Boolean = false)

object Reference {
  implicit val reads: Reads[Reference] =
    ((JsPath \ "value").read[String] and
      ((JsPath \ "isEditable").read[Boolean] orElse Reads.pure(false))
      ) (Reference.apply _)

  implicit val writes: Writes[Reference] = Json.writes[Reference]

  def applyEditable(vat: String): Reference = {
    Reference(vat)
  }

  def unapplyEditable(reference: Reference): Option[String] = {
    Some(reference.value)
  }
}


