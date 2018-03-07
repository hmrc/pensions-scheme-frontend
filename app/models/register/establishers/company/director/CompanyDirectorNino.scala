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

package models.register.establishers.company.director

import play.api.libs.json._
import utils.InputOption

sealed trait CompanyDirectorNino

object CompanyDirectorNino {

  case class Yes(nino: String) extends CompanyDirectorNino
  case class No(reason: String) extends CompanyDirectorNino

  def options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("companyDirectorNino_nino-form")),
    InputOption("false", "site.no", Some("companyDirectorNino_reason-form"))
  )

  implicit val reads: Reads[CompanyDirectorNino] = {

    (JsPath \ "hasNino").read[Boolean].flatMap {

      case true =>
        (JsPath \ "nino").read[String]
          .map[CompanyDirectorNino](Yes.apply)
          .orElse(Reads[CompanyDirectorNino](_ => JsError("NINO Value expected")))

      case false =>
        (JsPath \ "reason").read[String]
          .map[CompanyDirectorNino](No.apply)
          .orElse(Reads[CompanyDirectorNino](_ => JsError("Reason expected")))
    }
  }

  implicit lazy val writes = new Writes[CompanyDirectorNino] {
    def writes(o: CompanyDirectorNino) = {
      o match {
        case CompanyDirectorNino.Yes(nino) =>
          Json.obj("hasNino" -> true, "nino" -> nino)
        case CompanyDirectorNino.No(reason) =>
          Json.obj("hasNino" -> false, "reason" -> reason)
      }
    }
  }
}


