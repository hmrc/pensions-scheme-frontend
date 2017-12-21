/*
 * Copyright 2017 HM Revenue & Customs
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
import utils.{InputOption, WithName}

sealed trait SchemeType

object SchemeType {

  case object SingleTrust extends WithName("single") with SchemeType

  case object GroupLifeDeath extends WithName("group") with SchemeType

  case object BodyCorporate extends WithName("corp") with SchemeType

  case class Other(schemeTypeDetails: String) extends WithName("other") with SchemeType

  val other = "other"
  val mappings: Map[String, SchemeType] = Seq(
    SingleTrust,
    GroupLifeDeath,
    BodyCorporate
  ).map(v => (v.toString, v)).toMap

  def options: Seq[InputOption] = Seq(
    InputOption(SingleTrust.toString, s"scheme_details__type__${SingleTrust.toString}"),
    InputOption(GroupLifeDeath.toString, s"scheme_details__type__${GroupLifeDeath.toString}"),
    InputOption(BodyCorporate.toString, s"scheme_details__type__${BodyCorporate.toString}"),
    InputOption(other, s"scheme_details__type_${other}_more", Some("schemeType_schemeTypeDetails-form"))
  )

  implicit val reads: Reads[SchemeType] = {

    (JsPath \ "name").read[String].flatMap {

      case schemeTypeName if schemeTypeName == other =>
        (JsPath \ "schemeTypeDetails").read[String]
          .map[SchemeType](Other.apply)
          .orElse(Reads[SchemeType](_ => JsError("Other Value expected")))

      case schemeTypeName if mappings.keySet.contains(schemeTypeName) =>
        Reads(_ => JsSuccess(mappings.apply(schemeTypeName)))

      case _ => Reads(_ => JsError("Invalid Scheme Type"))
    }
  }

  implicit lazy val writes = new Writes[SchemeType] {
    def writes(o: SchemeType) = {
      o match {
        case SchemeType.Other(schemeTypeDetails) =>
          Json.obj("name" -> other, "schemeTypeDetails" -> schemeTypeDetails)
        case s if mappings.keySet.contains(s.toString) =>
          Json.obj("name" -> s.toString)
      }
    }
  }
}

