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

import play.api.libs.json.{Reads, Writes}
import scala.util.Try

case class EstablisherDetailsMap(private val data: Map[Int, EstablisherDetails]){

  def get(index: Int): Try[Option[EstablisherDetails]] =
    Try {
      require(index <= data.size + 1 && index <= 10)
      data.get(index)
    }
}

object EstablisherDetailsMap {

  implicit def reads(implicit ev: Reads[Map[Int, EstablisherDetails]]): Reads[EstablisherDetailsMap] =
    ev.map(EstablisherDetailsMap.apply)

  implicit def writes(implicit ev: Writes[Map[Int, EstablisherDetails]]): Writes[EstablisherDetailsMap] =
    Writes {
      model =>
        ev.writes(model.data)
    }
}
