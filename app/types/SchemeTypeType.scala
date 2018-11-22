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

package types

import play.api.libs.json._
import utils.{InputOption, WithName}

sealed trait SchemeTypeType

object SchemeTypeType {

  case object SingleTrust extends WithName("single") with SchemeTypeType

  case object GroupLifeDeath extends WithName("group") with SchemeTypeType

  case object BodyCorporate extends WithName("corp") with SchemeTypeType

  case object MasterTrust extends WithName("master") with SchemeTypeType

  case class Other(schemeTypeDetails: String) extends WithName("other") with SchemeTypeType

  val other = "other"
  val mappings: Map[String, SchemeTypeType] = Seq(
    SingleTrust,
    GroupLifeDeath,
    BodyCorporate,
    MasterTrust
  ).map(v => (v.toString, v)).toMap

  def options: Seq[InputOption] = Seq(
    InputOption(
      SingleTrust.toString,
      s"messages__scheme_details__type_${SingleTrust.toString}",
      hint = Set("messages__scheme_details__type_single_hint")
    ),
    InputOption(
      GroupLifeDeath.toString,
      s"messages__scheme_details__type_${GroupLifeDeath.toString}",
      hint = Set("messages__scheme_details__type_group_hint")
    ),
    InputOption(
      BodyCorporate.toString,
      s"messages__scheme_details__type_${BodyCorporate.toString}",
      hint = Set("messages__scheme_details__type_corp_hint")
    ),
    InputOption(
      MasterTrust.toString,
      s"messages__scheme_details__type_${MasterTrust.toString}",
      hint = Set("messages__scheme_details__type_master_hint")
    ),
    InputOption(
      other,
      s"messages__scheme_details__type_$other",
      Some("schemeType_schemeTypeDetails-form"),
      hint = Set("messages__scheme_details__type_other_hint")
    )
  )

  implicit val reads: Reads[SchemeTypeType] = {

    (JsPath \ "name").read[String].flatMap {

      case schemeTypeName if schemeTypeName == other =>
        (JsPath \ "schemeTypeDetails").read[String]
          .map[SchemeTypeType](Other.apply)
          .orElse(Reads[SchemeTypeType](_ => JsError("Other Value expected")))

      case schemeTypeName if mappings.keySet.contains(schemeTypeName) =>
        Reads(_ => JsSuccess(mappings.apply(schemeTypeName)))

      case _ => Reads(_ => JsError("Invalid Scheme Type"))
    }
  }

  implicit lazy val writes = new Writes[SchemeTypeType] {
    def writes(o: SchemeTypeType) = {
      o match {
        case SchemeTypeType.Other(schemeTypeDetails) =>
          Json.obj("name" -> other, "schemeTypeDetails" -> schemeTypeDetails)
        case s if mappings.keySet.contains(s.toString) =>
          Json.obj("name" -> s.toString)
      }
    }
  }

  def getSchemeType(schemeTypeStr : Option[String], isMasterTrust: Boolean): Option[String] = {
    if (isMasterTrust) {
      Some(s"messages__scheme_details__type_${MasterTrust.toString}")
    } else {
      schemeTypeStr.flatMap{ schemeStr =>
        List(SingleTrust.toString, GroupLifeDeath.toString, BodyCorporate.toString, other).find(scheme=>
          schemeStr.toLowerCase.contains(scheme.toLowerCase)).map{ str =>
          s"messages__scheme_details__type_${str}"
        }
      }
    }
  }
}
