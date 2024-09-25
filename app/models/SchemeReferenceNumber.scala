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

case class SchemeReferenceNumber(id: String){
  implicit def optionPathBindable(implicit stringBinder: PathBindable[String]): PathBindable[Option[SchemeReferenceNumber]] = new PathBindable[Option[SchemeReferenceNumber]] {
    override def bind(key: String, value: String): Either[String, Option[SchemeReferenceNumber]] = {
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
      srnOpt.map(_.id).getOrElse("")
    }
  }
}

object SchemeReferenceNumber {


  val regexSRN: Regex = "^S[0-9]{10}$".r

  // PathBindable for SchemeReferenceNumber
  implicit def srnPathBindable(implicit stringBinder: PathBindable[String]): PathBindable[SchemeReferenceNumber] = new PathBindable[SchemeReferenceNumber] {

    val regexSRN: Regex = "^S[0-9]{10}$".r

    override def bind(key: String, value: String): Either[String, SchemeReferenceNumber] = {

      val pattern = """SchemeReferenceNumber\((.*?)\)""".r

      val result = value match {
        case pattern(schemeRefNumber) => schemeRefNumber
        case _ => value
      }

      stringBinder.bind(key, result) match {
        case Right(srn@regexSRN(_*)) =>
          Right(SchemeReferenceNumber(srn))
        case _ =>
          Left("SchemeReferenceNumber binding failed")
      }
    }

    override def unbind(key: String, value: SchemeReferenceNumber): String = {
      stringBinder.unbind(key, value.id)
    }
  }




  // QueryBindable for SchemeReferenceNumber
  implicit def queryBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[SchemeReferenceNumber] =
    new QueryStringBindable[SchemeReferenceNumber] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SchemeReferenceNumber]] = {
        stringBinder.bind(key, params) match {
          case Some(Right(id)) => Some(Right(SchemeReferenceNumber(id)))
          case Some(Left(error)) => Some(Left(error))
          case None => None
        }
      }

      override def unbind(key: String, value: SchemeReferenceNumber): String = {
        stringBinder.unbind(key, value.id)
      }
    }



  // QueryBindable for Option[SchemeReferenceNumber]
  implicit def optionQueryBindable(implicit schemeRefBinder: QueryStringBindable[SchemeReferenceNumber]): QueryStringBindable[Option[SchemeReferenceNumber]] =
    new QueryStringBindable[Option[SchemeReferenceNumber]] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Option[SchemeReferenceNumber]]] = {

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
        value.map(schemeRefBinder.unbind(key, _)).getOrElse(s"$key=noSRN")
      }
    }

  implicit def schemeReferenceNumberToString(srn: SchemeReferenceNumber): String =
    srn.id

  implicit def stringToSchemeReferenceNumber(srn: SchemeReferenceNumber): SchemeReferenceNumber = {
    SchemeReferenceNumber(srn)
  }

  case class InvalidSchemeReferenceNumberException() extends Exception

  implicit val format: OFormat[SchemeReferenceNumber] = Json.format[SchemeReferenceNumber]



  implicit val jsLiteralOptionSchemeRef: JavascriptLiteral[Option[SchemeReferenceNumber]] =
    new JavascriptLiteral[Option[SchemeReferenceNumber]] {
      override def to(value: Option[SchemeReferenceNumber]): String = {
        value match {
          case Some(schemeRef) => s"${schemeRef.id}"
          case None => ""
        }
      }
    }


  implicit val jsLiteralOptionSchemeRef2: JavascriptLiteral[Some[SchemeReferenceNumber]] =
    new JavascriptLiteral[Some[SchemeReferenceNumber]] {
      override def to(value: Some[SchemeReferenceNumber]): String = {
        value match {
          case Some(schemeRef) => s"${schemeRef.id}"
          case _ => ""
        }
      }
    }

  implicit val jsLiteralSchemeRef: JavascriptLiteral[SchemeReferenceNumber] =
    new JavascriptLiteral[SchemeReferenceNumber] {
      override def to(value: SchemeReferenceNumber): String = {
        value.id
      }
   }
}