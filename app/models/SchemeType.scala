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
import utils.WithName

sealed trait SchemeType

object SchemeType {

  case object SingleTrust extends WithName("singleTrust") with SchemeType

  case object GroupLifeDeath extends WithName("groupLifeDeath") with SchemeType

  case object BodyCorporate extends WithName("bodyCorporate") with SchemeType

  case class Other(schemeTypeDetails: String) extends WithName("other") with SchemeType

  val mappings : Map[String, SchemeType] = Seq(
    SingleTrust,
    GroupLifeDeath,
    BodyCorporate
  ).map(v => (v.toString, v)).toMap

  implicit val reads: Reads[SchemeType] =  {
    (JsPath \ "name").read[String].flatMap {
      case s if s == "other" =>
        (JsPath \ "otherValue").read[String]
          .map[SchemeType](Other.apply)
          .orElse(Reads[SchemeType](_ => JsError("Other Value expected")))
      case s if mappings.keySet.contains(s) => {
        Reads(_ => JsSuccess(mappings.apply(s)))
      }
      case _ => Reads(_ => JsError("Invalid Scheme Type"))
    }
  }

  implicit lazy val writes: Writes[SchemeType] = ???

}

