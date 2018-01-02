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

class SchemeTypeSpec extends WordSpecLike with MustMatchers with OptionValues {

  "Reads" must {
    "successfully read Other" in {
      val json = Json.obj("name" -> "other", "schemeTypeDetails" -> "some value")

      Json.fromJson[SchemeType](json).asOpt.value mustEqual SchemeType.Other("some value")
    }

    "successfully read Single Trust" in {
      val json = Json.obj("name" -> "single")

      Json.fromJson[SchemeType](json).asOpt.value mustEqual SchemeType.SingleTrust
    }

    "successfully read Group Life Death" in {
      val json = Json.obj("name" -> "group")

      Json.fromJson[SchemeType](json).asOpt.value mustEqual SchemeType.GroupLifeDeath
    }

    "successfully read Body Corporate" in {
      val json = Json.obj("name" -> "corp")

      Json.fromJson[SchemeType](json).asOpt.value mustEqual SchemeType.BodyCorporate
    }

    "return failure for Invalid scheme type" in {
      val json = Json.obj("name" -> "invalidSchemeType")

      Json.fromJson[SchemeType](json) mustEqual JsError("Invalid Scheme Type")
    }

    "return failure for other without other value" in {
      val json = Json.obj("name" -> "other")

      Json.fromJson[SchemeType](json) mustEqual JsError("Other Value expected")
    }
  }

  "Writes" must {
    "return successfully write single trust" in {
      Json.toJson[SchemeType](SchemeType.SingleTrust) mustEqual Json.obj("name" -> "single")
    }
    "return successfully write GroupLifeDeath" in {
      Json.toJson[SchemeType](SchemeType.GroupLifeDeath) mustEqual Json.obj("name" -> "group")
    }
    "return successfully write body Corporate" in {
      Json.toJson[SchemeType](SchemeType.BodyCorporate) mustEqual Json.obj("name" -> "corp")
    }
    "return successfully write other" in {
      Json.toJson[SchemeType](SchemeType.Other("Some Scheme")) mustEqual Json.obj("name" -> "other", "schemeTypeDetails"->"Some Scheme")
    }
  }
}