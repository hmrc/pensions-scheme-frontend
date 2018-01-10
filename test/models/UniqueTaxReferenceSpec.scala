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

package models

import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}
import play.api.libs.json.{JsError, Json}

class UniqueTaxReferenceSpec extends WordSpecLike with MustMatchers with OptionValues {

  "Reads" must {
    "successfully read Yes" in {
      val json = Json.obj("hasUtr" -> "yes", "utr" -> "1234567891")

      Json.fromJson[UniqueTaxReference](json).asOpt.value mustEqual UniqueTaxReference.Yes("1234567891")
    }

    "successfully read No" in {
      val json = Json.obj("hasUtr" -> "no", "reason" -> "haven't got utr")

      Json.fromJson[UniqueTaxReference](json).asOpt.value mustEqual UniqueTaxReference.No("haven't got utr")
    }

    "return failure for Invalid scheme type" in {
      val json = Json.obj("hasUtr" -> "invalidSelection")

      Json.fromJson[UniqueTaxReference](json) mustEqual JsError("Invalid selection")
    }

    "return failure for yes without utr" in {
      val json = Json.obj("hasUtr" -> "yes")

      Json.fromJson[UniqueTaxReference](json) mustEqual JsError("Utr Value expected")
    }

    "return failure for no without reason" in {
      val json = Json.obj("hasUtr" -> "no")

      Json.fromJson[UniqueTaxReference](json) mustEqual JsError("Reason expected")
    }
  }

  "Writes" must {
    "return successfully write Yes" in {
      Json.toJson[UniqueTaxReference](UniqueTaxReference.Yes("1234567891")) mustEqual Json.obj("hasUtr" -> "yes", "utr" -> "1234567891")
    }
    "return successfully write No" in {
      Json.toJson[UniqueTaxReference](UniqueTaxReference.No("haven't got utr")) mustEqual Json.obj("hasUtr" -> "no", "reason" -> "haven't got utr")
    }
  }
}
