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

package models.register.establishers.individual

import models.AddressYears
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.libs.json._
import utils.{Enumerable, MapFormats}

import scala.util.Success

class EstablishersIndividualMapSpec extends WordSpecLike with MustMatchers with MapFormats with Enumerable.Implicits {

  "addressYearsMap writes" must {
    "write correctly formatted JSON" in {
      val testMap: EstablishersIndividualMap[AddressYears] =
        EstablishersIndividualMap(Map(0 -> AddressYears.UnderAYear, 1 -> AddressYears.OverAYear))

      val expectedData = Json.obj(
        "0" -> "under_a_year",
        "1" -> "over_a_year"
      )
      val result = Json.toJson[EstablishersIndividualMap[AddressYears]](testMap)
      result mustEqual expectedData
    }
  }

  "addressYearsMap read" must {
    "read successfully AddressYearsMap" in {

      val expectedData: EstablishersIndividualMap[AddressYears] =
        EstablishersIndividualMap(Map(0 -> AddressYears.UnderAYear, 1 -> AddressYears.OverAYear))

      val testJson = Json.obj(
        "0" -> "under_a_year",
        "1" -> "over_a_year"
      )
      val result = Json.fromJson[EstablishersIndividualMap[AddressYears]](testJson).get
      result mustEqual expectedData
    }
  }

  "get" must {
    "return the appropriate Address Years for the given index" in {
      val testMap: EstablishersIndividualMap[AddressYears] =
        EstablishersIndividualMap(Map(0 -> AddressYears.UnderAYear, 1 -> AddressYears.OverAYear))

      testMap.get(0) mustEqual Success(Some(AddressYears.UnderAYear))
    }

    "return None for the valid index but don't have relevant data" in {
      val testMap: EstablishersIndividualMap[AddressYears] =
        EstablishersIndividualMap(Map(0 -> AddressYears.UnderAYear, 1 -> AddressYears.OverAYear))

      testMap.get(2) mustEqual Success(None)
    }

    "return failure if the index is invalid" in {
      val testMap: EstablishersIndividualMap[AddressYears] =
        EstablishersIndividualMap(Map(0 -> AddressYears.UnderAYear, 1 -> AddressYears.OverAYear))

      testMap.get(10).isFailure mustEqual true
    }
  }
}
