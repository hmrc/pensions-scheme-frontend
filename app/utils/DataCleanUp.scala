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
  private def isEmptyCheck(jsValue: JsValue, defName: String): Boolean = {
    val check: Boolean =
      jsValue.as[JsObject].keys.isEmpty

    logger.info(s"$defName empty json check returned $check")

    check
  }

  private def subsetCheck(jsValue: JsValue, keySet: Set[String], defName: String): Boolean = {
    val check: Boolean =
      jsValue.as[JsObject].keys.size.equals(keySet.size) && jsValue.as[JsObject].keys.subsetOf(keySet)

    logger.info(s"$defName subset keys check returned $check")

    check
  }

  def filterNotEmptyObjectsAndSubsetKeys(jsArray: JsArray, keySet: Set[String], defName: String): collection.IndexedSeq[JsValue] =
    jsArray
      .value
      .filterNot { jsValue =>
        isEmptyCheck(jsValue, defName) || subsetCheck(jsValue, keySet, defName)
      }
}
