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

package models

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}
import play.api.mvc.{PathBindable, QueryStringBindable}

import scala.language.implicitConversions

case class SchemeReferenceNumber(srn: Option[String])

object SchemeReferenceNumber {

  implicit def schemeReferenceNumberPathBindable(implicit optionalBinder: PathBindable[Option[String]]):
  PathBindable[SchemeReferenceNumber] = new PathBindable[SchemeReferenceNumber] {

    override def bind(key: String, value: String): Either[String, SchemeReferenceNumber] = {
      optionalBinder.bind(key, value) match {
        case Right(x) if x.isEmpty=> Right(SchemeReferenceNumber(None))
        case Right(x)=> Right(SchemeReferenceNumber(x))
        case _ => Left("SchemeReferenceNumber binding failed")
      }
    }

    override def unbind(key: String, value: SchemeReferenceNumber): String = {
      optionalBinder.unbind(key, value.srn)
    }
  }

}
