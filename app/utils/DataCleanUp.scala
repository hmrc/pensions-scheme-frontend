/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.Logging
import play.api.libs.json.*

import scala.collection.Set

object DataCleanUp extends Logging {

  def filterNotEmptyObjectsAndSubsetKeys(jsArray: JsArray, keySet: Set[String], defName: String): collection.IndexedSeq[JsValue] = {
    val filteredCollection: collection.IndexedSeq[JsValue] =
      jsArray
        .value
        .filterNot { jsValue =>
          jsValue.as[JsObject].keys.isEmpty ||
            (jsValue.as[JsObject].keys.size.equals(keySet.size) && jsValue.as[JsObject].keys.subsetOf(keySet))
        }

    val removed = jsArray.value.size - filteredCollection.size

    if (removed > 0) logger.info(s"$defName: $removed elements removed")

    filteredCollection
  }
}
