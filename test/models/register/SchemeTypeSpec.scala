/*
 * Copyright 2023 HM Revenue & Customs
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

package models.register

import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, Json}

class SchemeTypeSpec extends AnyWordSpecLike with Matchers with OptionValues {

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
      Json.toJson[SchemeType](SchemeType.Other("Some Scheme")) mustEqual Json.obj("name" -> "other", "schemeTypeDetails" -> "Some Scheme")
    }
  }

  "getSchemetype" must{

    "return master trust type if isMasterTrust is true" in {
      SchemeType.getSchemeType(None, isMasterTrust = true) mustEqual Some("messages__scheme_details__type_master")
    }

    "return type_single if isMasterTrust is false and scheme type is present as part" in {
      val str = "A single trust under which all of the assets are held for the benefit of all members of the scheme"
      SchemeType.getSchemeType(Some(str), isMasterTrust = false) mustEqual Some("messages__scheme_details__type_single")
    }

    "return type_group if isMasterTrust is false and scheme type is present as part" in {
      val str = "A group life/death in service scheme"
      SchemeType.getSchemeType(Some(str), isMasterTrust = false) mustEqual Some("messages__scheme_details__type_group")
    }

    "return type_body if isMasterTrust is false and scheme type is present as part" in {
      val str = "A body corporate"
      SchemeType.getSchemeType(Some(str), isMasterTrust = false) mustEqual Some("messages__scheme_details__type_corp")
    }

    "return type_other if isMasterTrust is false and scheme type is present as part" in {
      val str = "Other"
      SchemeType.getSchemeType(Some(str), isMasterTrust = false) mustEqual Some("messages__scheme_details__type_other")
    }
  }
}
