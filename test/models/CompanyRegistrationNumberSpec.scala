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

import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}
import play.api.libs.json.{JsError, JsPath, Json}
import reactivemongo.play.json.ValidationError

class CompanyRegistrationNumberSpec extends WordSpecLike with MustMatchers with OptionValues {

  "Reads" must {
    "successfully read true" in {
      val json = Json.obj("hasCrn" -> true, "crn" -> "1234567")

      Json.fromJson[CompanyRegistrationNumber](json).asOpt.value mustEqual CompanyRegistrationNumber.Yes("1234567")
    }

    "successfully read false" in {
      val json = Json.obj("hasCrn" -> false, "reason" -> "haven't got CRN")

      Json.fromJson[CompanyRegistrationNumber](json).asOpt.value mustEqual CompanyRegistrationNumber.No("haven't got CRN")
    }

    "return failure for true without CRN" in {
      val json = Json.obj("hasCrn" -> true)

      Json.fromJson[CompanyRegistrationNumber](json) mustEqual JsError("CRN Value expected")
    }

    "return failure for false without reason" in {
      val json = Json.obj("hasCrn" -> false)

      Json.fromJson[CompanyRegistrationNumber](json) mustEqual JsError("Reason expected")
    }

    "return failure for when no input given" in {
      val json = Json.obj("hasCrn" -> "notABoolean")
      Json.fromJson[CompanyRegistrationNumber](json) mustEqual JsError(Seq((JsPath \ "hasCrn", Seq(ValidationError(Seq("error.expected.jsboolean"), Seq())))))
    }
  }

  "Writes" must {
    "return successfully write Yes" in {
      Json.toJson[CompanyRegistrationNumber](CompanyRegistrationNumber.Yes("1234567")) mustEqual Json.obj("hasCrn" -> true, "crn" -> "1234567")
    }
    "return successfully write No" in {
      Json.toJson[CompanyRegistrationNumber](CompanyRegistrationNumber.No("haven't got CRN")) mustEqual Json.obj("hasCrn" -> false, "reason" -> "haven't got CRN")
    }
  }

}
