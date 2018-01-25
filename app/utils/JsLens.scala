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

package utils

import play.api.libs.json._

trait JsLens {
  self =>

  def get(outer: JsValue): JsResult[JsValue]
  def put(outer: JsValue, inner: JsValue): JsResult[JsValue]

  def andThen(other: JsLens): JsLens = {

    def get(outer: JsValue): JsResult[JsValue] =
      self.get(outer).flatMap(other.get)

    def put(outer0: JsValue, inner0: JsValue): JsResult[JsValue] = {
      for {
        outer <- self.get(outer0).recover {
          case e if e.errors.exists {
            _._2.exists {
              error =>
                error.message.contains("undefined") ||
                  error.message.contains("out of bounds")
            }
          } => JsNull
        }
        inner <- other.put(outer, inner0)
        newOuter <- self.put(outer0, inner)
      } yield newOuter
    }

    JsLens(get)(put)
  }
}

object JsLens {

  val id: JsLens = {

    def get(outer: JsValue): JsResult[JsValue] =
      JsSuccess(outer)

    def put(outer: JsValue, inner: JsValue): JsResult[JsValue] =
      JsSuccess(inner)

    JsLens(get)(put)
  }

  def apply(get0: JsValue => JsResult[JsValue])(put0: (JsValue, JsValue) => JsResult[JsValue]): JsLens =
    new JsLens {
      override def get(outer: JsValue): JsResult[JsValue] =
        get0(outer)
      override def put(outer: JsValue, inner: JsValue): JsResult[JsValue] =
        put0(outer, inner)
    }

  def atKey(key: String): JsLens = {

    def get(outer: JsValue): JsResult[JsValue] = {
      (outer \ key).validate[JsValue]
    }

    def put(outer: JsValue, inner: JsValue): JsResult[JsValue] = {
      outer match {
        case obj: JsObject =>
          JsSuccess(obj ++ Json.obj(key -> inner))
        case JsNull =>
          JsSuccess(Json.obj(key -> inner))
        case _ =>
          JsError("Not an object")
      }
    }

    JsLens(get)(put)
  }

  def atIndex(index: Int): JsLens = {
    require(index >= 0)

    def get(outer: JsValue): JsResult[JsValue] = {
      (outer \ index).validate[JsValue]
    }

    def put(outer: JsValue, inner: JsValue): JsResult[JsValue] = {
      outer match {
        case JsArray(values) if index < values.size  =>
          JsSuccess(JsArray(values.patch(index, Seq(inner), 1)))
        case JsArray(values) if index == values.size =>
          JsSuccess(JsArray(values :+ inner))
        case JsNull if index == 0 =>
          JsSuccess(JsArray(Seq(inner)))
        case JsArray(_) =>
          JsError("Index out of bounds")
        case _ =>
          JsError("Not an array")
      }
    }

    JsLens(get)(put)
  }

  def fromPath(path: JsPath): JsLens = {

    def toLens(node: PathNode): JsLens = {
      node match {
        case KeyPathNode(key) =>
          JsLens.atKey(key)
        case IdxPathNode(idx) =>
          JsLens.atIndex(idx)
        case _ =>
          throw new RuntimeException("Path wildcards aren't allowed")
      }
    }

    path.path.foldLeft(id) {
      case (lens, node) =>
        lens andThen toLens(node)
    }
  }
}

