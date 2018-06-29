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
import play.api.libs.functional.syntax._

case class CompanyDetails(companyName: String, vatNumber: Option[String], payeNumber: Option[String], isDeleted: Boolean = false)

object CompanyDetails {
  implicit val reads: Reads[CompanyDetails] =
    ((JsPath \ "companyName").read[String] and
      (JsPath \ "vatNumber").readNullable[String] and
      (JsPath \ "payeNumber").readNullable[String] and
      ((JsPath \ "isDeleted").read[Boolean] orElse (Reads.pure(false)))
      ) (CompanyDetails.apply _)

  implicit val writes: Writes[CompanyDetails] = Json.writes[CompanyDetails]

  def applyDelete(companyName: String, vatNumber: Option[String], payeNumber: Option[String]): CompanyDetails = {
    CompanyDetails(companyName, vatNumber, payeNumber, false)
  }

  def unapplyDelete(companyDetails: CompanyDetails): Option[(String, Option[String], Option[String])] = {
    Some((companyDetails.companyName, companyDetails.vatNumber, companyDetails.payeNumber))
  }
}
