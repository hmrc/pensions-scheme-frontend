/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait MapFormats {

  implicit def intMapReads[V](implicit ev: Reads[Map[String, V]]): Reads[Map[Int, V]] = {
    JsPath.read[Map[String, V]].flatMap {
      m =>
        Try {
          m.map {
            case (k, v) =>
              k.toInt -> v
          }
        } match {
          case Success(map) =>
            Reads(_ => JsSuccess(map))
          case Failure(_) =>
            Reads(_ => JsError("Invalid key type"))
        }
    }
  }

  implicit def intMapWrites[V](implicit ev: Writes[Map[String, V]]): Writes[Map[Int, V]] =
    Writes {
      map =>
        Json.toJson {
          map.map {
            case (k, v) =>
              k.toString -> v
          }
        }
    }
}
