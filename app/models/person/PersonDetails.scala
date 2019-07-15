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

case class PersonDetails(firstName: String, middleName: Option[String], lastName: String, date: LocalDate, isDeleted: Boolean = false) {

  def firstAndLastName: String = s"$firstName $lastName"

  def fullName: String = middleName match {
    case Some(middle) => s"$firstName $middle $lastName"
    case _ => s"$firstName $lastName"
  }
}

object PersonDetails {
  implicit val reads: Reads[PersonDetails] =
    ((JsPath \ "firstName").read[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "date").read[LocalDate] and
      ((JsPath \ "isDeleted").read[Boolean] orElse (Reads.pure(false)))
      ) (PersonDetails.apply _)

  implicit val writes: Writes[PersonDetails] = Json.writes[PersonDetails]

  def applyDelete(firstName: String, middleName: Option[String], lastName: String, date: LocalDate): PersonDetails = {
    PersonDetails(firstName, middleName, lastName, date, false)
  }

  def unapplyDelete(personDetails: PersonDetails): Option[(String, Option[String], String, LocalDate)] = {
    Some((personDetails.firstName, personDetails.middleName, personDetails.lastName, personDetails.date))
  }
}
