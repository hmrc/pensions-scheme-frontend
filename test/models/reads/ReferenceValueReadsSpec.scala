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

package models.reads

import models.ReferenceValue
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class ReferenceValueReadsSpec extends AnyWordSpec with Matchers with OptionValues {
  "ReferenceValue json" should {

    "map correctly to Reference" when {

      "We have ReferenceValue with isEditable defaulted to false when no isEditable flag is in json" in {
        val payload = Json.obj("value" -> "test")
        val result = payload.as[ReferenceValue]
        result.isEditable mustBe false
      }

      "We have ReferenceValue with isEditable flag to true when isEditable is present in json" in {
        val payload = Json.obj("value" -> "test", "isEditable" -> true)
        val result = payload.as[ReferenceValue]
        result.isEditable mustBe true
      }
    }
  }
}
