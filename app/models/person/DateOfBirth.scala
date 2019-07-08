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

package models.person

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class DateOfBirth(date: LocalDate, isDeleted: Boolean = false)

object DateOfBirth {
  implicit val reads: Reads[DateOfBirth] =
    (
      (JsPath \ "date").read[LocalDate] and
        ((JsPath \ "isDeleted").read[Boolean] orElse Reads.pure(false))
      ) (DateOfBirth.apply _)

  implicit val writes: Writes[DateOfBirth] = Json.writes[DateOfBirth]

  def applyDelete(date: LocalDate): DateOfBirth = {
    DateOfBirth(date)
  }

  def unapplyDelete(DateOfBirth: DateOfBirth): Option[LocalDate] = {
    Some(DateOfBirth.date)
  }
}

