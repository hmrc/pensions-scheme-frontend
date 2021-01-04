/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsError, JsNumber, Json}

class MapFormatsSpec extends WordSpec with MustMatchers with MapFormats {

  ".intMapWrites" must {
    "write correctly formatted JSON" in {
      val testMap: Map[Int, Int] = Map(
        1 -> 2, 3 -> 28
      )
      val expectedData = Json.obj(
        "1" -> JsNumber(2),
        "3" -> JsNumber(28)
      )
      val result = Json.toJson(testMap)
      result mustEqual expectedData
    }
  }

  ".intMapReads" must {
    "read the json successfully" in {
      val testJson = Json.obj(
        "1" -> JsNumber(2),
        "3" -> JsNumber(28)
      )
      val objResult: Map[Int, Int] = Map(
        1 -> 2, 3 -> 28
      )

      Json.fromJson[Map[Int, Int]](testJson).get mustEqual objResult
    }

    "failure to read the json" in {
      val testJson = Json.obj(
        "1.5" -> JsNumber(1)
      )

      Json.fromJson[Map[Int, Int]](testJson) mustEqual JsError("Invalid key type")
    }
  }
}
