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


  val regexSRN: Regex = "^S[0-9]{10}$".r

  // PathBindable for SchemeReferenceNumber
  implicit def srnPathBindable(implicit stringBinder: PathBindable[String]): PathBindable[SchemeReferenceNumber] = new PathBindable[SchemeReferenceNumber] {

    val regexSRN: Regex = "^S[0-9]{10}$".r

    override def bind(key: String, value: String): Either[String, SchemeReferenceNumber] = {
      println(s"************in srnPathBindable path bind $key $value")

      val pattern = """SchemeReferenceNumber\((.*?)\)""".r

      val result = value match {
        case pattern(schemeRefNumber) => schemeRefNumber
        case _ => value
      }

      stringBinder.bind(key, result) match {
        case Right(srn@regexSRN(_*)) =>
          Right(SchemeReferenceNumber(srn))
        case x =>
          println(s"what is this srn!!! $x")
          Left("SchemeReferenceNumber binding failed")
      }
    }

    override def unbind(key: String, value: SchemeReferenceNumber): String = {
      println(s"************in srnPathBindable path unbind $key $value")
      stringBinder.unbind(key, value.id)
    }
  }

  implicit def optionPathBindable(implicit stringBinder: PathBindable[String]): PathBindable[Option[SchemeReferenceNumber]] = new PathBindable[Option[SchemeReferenceNumber]] {
    override def bind(key: String, value: String): Either[String, Option[SchemeReferenceNumber]] = {
      println(s"************in optionPathBindable path bind $key $value")
      if (value.isEmpty) {
        Right(None)
      } else {
        stringBinder.bind(key, value) match {
          case Right(v) => Right(Some(SchemeReferenceNumber(v)))
          case Left(error) => Left(error)
        }
      }
    }

    override def unbind(key: String, srnOpt: Option[SchemeReferenceNumber]): String = {
      println(s"************in optionPathBindable unbind $key $srnOpt")
      srnOpt.map(_.id).getOrElse("")
    }
  }


  // QueryBindable for SchemeReferenceNumber
  implicit def queryBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[SchemeReferenceNumber] =
    new QueryStringBindable[SchemeReferenceNumber] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SchemeReferenceNumber]] = {
        println(s"************in queryBindable bind $key $params")

        stringBinder.bind(key, params) match {
          case Some(Right(id)) => Some(Right(SchemeReferenceNumber(id)))
          case Some(Left(error)) => Some(Left(error))
          case None => None
        }
      }

      override def unbind(key: String, value: SchemeReferenceNumber): String = {
        println(s"************in queryBindable unbind $key $value")

        stringBinder.unbind(key, value.id)
      }
    }



  // QueryBindable for Option[SchemeReferenceNumber]
  implicit def optionQueryBindable(implicit schemeRefBinder: QueryStringBindable[SchemeReferenceNumber]): QueryStringBindable[Option[SchemeReferenceNumber]] =
    new QueryStringBindable[Option[SchemeReferenceNumber]] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Option[SchemeReferenceNumber]]] = {
        println(s"in optionQueryBindable bind $key $params")

        params.get(key) match {
          case Some(Seq("")) => Some(Right(None))
          case Some(Seq("noSRN")) =>
            Some(Right(None))
          case Some(_) => schemeRefBinder.bind(key, params).map {
            case Right(schemeRef) => Right(Some(schemeRef))
            case Left(error) => Left(error)
          }
          case None => None
        }
      }

      override def unbind(key: String, value: Option[SchemeReferenceNumber]): String = {
        println(s"in optionQueryBindable unbind $key $value")

        value.map(schemeRefBinder.unbind(key, _)).getOrElse(s"$key=noSRN")
      }
    }

  implicit def schemeReferenceNumberToString(srn: SchemeReferenceNumber): String =
    srn.id

  implicit def stringToSchemeReferenceNumber(srn: SchemeReferenceNumber): SchemeReferenceNumber = {
    println(s"************* stringToSchemeReferenceNumber $srn")
    SchemeReferenceNumber(srn)
  }

  case class InvalidSchemeReferenceNumberException() extends Exception

  implicit val format: OFormat[SchemeReferenceNumber] = Json.format[SchemeReferenceNumber]



  implicit val jsLiteralOptionSchemeRef: JavascriptLiteral[Option[SchemeReferenceNumber]] =
    new JavascriptLiteral[Option[SchemeReferenceNumber]] {
      def to(value: Option[SchemeReferenceNumber]): String = value match {
        case Some(schemeRef) => s"'${schemeRef.id}'"
        case None => ""
      }
    }

}