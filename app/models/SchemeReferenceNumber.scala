/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{JavascriptLiteral, PathBindable, QueryStringBindable}

import scala.language.implicitConversions
import scala.util.matching.Regex

case class SchemeReferenceNumber(id: String)

object SchemeReferenceNumber {

  implicit def srnPathBindable(implicit stringBinder: PathBindable[String]): PathBindable[SchemeReferenceNumber] = new PathBindable[SchemeReferenceNumber] {

    val regexSRN: Regex = "^S[0-9]{10}$".r

    override def bind(key: String, value: String): Either[String, SchemeReferenceNumber] = {
      stringBinder.bind(key, value) match {
        case Right(srn@regexSRN(_*)) => Right(SchemeReferenceNumber(srn))
        case _ => Left("SchemeReferenceNumber binding failed")
      }
    }

    override def unbind(key: String, value: SchemeReferenceNumber): String = {
      stringBinder.unbind(key, value.id)
    }
  }

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[SchemeReferenceNumber] =
    new QueryStringBindable[SchemeReferenceNumber] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SchemeReferenceNumber]] = {
        stringBinder.bind(key, params).map {
          case Right(value) => Right(SchemeReferenceNumber(value))
          case Left(error) => Left(error)
        }
      }

      override def unbind(key: String, value: SchemeReferenceNumber): String = {
        stringBinder.unbind(key, value.id)
      }
    }
      implicit def schemeReferenceNumberToString(srn: SchemeReferenceNumber): String =
        srn.id

      implicit def stringToSchemeReferenceNumber(srn: SchemeReferenceNumber): SchemeReferenceNumber =
        SchemeReferenceNumber(srn)

      case class InvalidSchemeReferenceNumberException() extends Exception

      implicit val format: OFormat[SchemeReferenceNumber] = Json.format[SchemeReferenceNumber]

      import play.api.routing._
      import play.api.routing.sird._

      implicit val jsLiteralOptionSchemeRef: JavascriptLiteral[Option[SchemeReferenceNumber]] =
        new JavascriptLiteral[Option[SchemeReferenceNumber]] {
          def to(value: Option[SchemeReferenceNumber]): String = value match {
            case Some(schemeRef) => s"'${schemeRef.id}'"
            case None => ""
          }
        }
}