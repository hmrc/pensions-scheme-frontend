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
import utils.{InputOption}

sealed trait CompanyRegistrationNumber

object CompanyRegistrationNumber {

  case class Yes(crn:String) extends CompanyRegistrationNumber
  case class No(reason:String) extends CompanyRegistrationNumber

  def options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("companyRegistrationNumber_crn-form")),
    InputOption("false", "site.no", Some("companyRegistrationNumber_reason-form"))
  )

  implicit val reads: Reads[CompanyRegistrationNumber] = {

    (JsPath \ "hasCrn").read[Boolean].flatMap {

      case true =>
        (JsPath \ "crn").read[String]
          .map[CompanyRegistrationNumber](Yes.apply)
          .orElse(Reads[CompanyRegistrationNumber](_ => JsError("CRN Value expected")))

      case false =>
        (JsPath \ "reason").read[String]
          .map[CompanyRegistrationNumber](No.apply)
          .orElse(Reads[CompanyRegistrationNumber](_ => JsError("Reason expected")))
    }
  }

  implicit lazy val writes = new Writes[CompanyRegistrationNumber] {
    def writes(o: CompanyRegistrationNumber) = {
      o match {
        case CompanyRegistrationNumber.Yes(crn) =>
          Json.obj("hasCrn" -> true, "crn" -> crn)
        case CompanyRegistrationNumber.No(reason) =>
          Json.obj("hasCrn" -> false, "reason" -> reason)
      }
    }
  }
}