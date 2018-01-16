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

class EstablisherNinoSpec extends WordSpecLike with MustMatchers with OptionValues {

  "Reads" must {
    "successfully read true" in {
      val json = Json.obj("hasNino" -> true, "nino" -> "AB020202A")

      Json.fromJson[EstablisherNino](json).asOpt.value mustEqual EstablisherNino.Yes("AB020202A")
    }

    "successfully read false" in {
      val json = Json.obj("hasNino" -> false, "reason" -> "haven't got Nino")

      Json.fromJson[EstablisherNino](json).asOpt.value mustEqual EstablisherNino.No("haven't got Nino")
    }

    "return failure for true without nino" in {
      val json = Json.obj("hasNino" -> true)

      Json.fromJson[EstablisherNino](json) mustEqual JsError("NINO Value expected")
    }

    "return failure for false without reason" in {
      val json = Json.obj("hasNino" -> false)

      Json.fromJson[EstablisherNino](json) mustEqual JsError("Reason expected")
    }
  }

  "Writes" must {
    "return successfully write Yes" in {
      Json.toJson[EstablisherNino](EstablisherNino.Yes("AB020202A")) mustEqual Json.obj("hasNino" -> true, "nino" -> "AB020202A")
    }
    "return successfully write No" in {
      Json.toJson[EstablisherNino](EstablisherNino.No("haven't got Nino")) mustEqual Json.obj("hasNino" -> false, "reason" -> "haven't got Nino")
    }
  }
}
