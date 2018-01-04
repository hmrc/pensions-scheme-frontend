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

import models.{EstablisherDetails, EstablisherDetailsMap}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.libs.json._
import utils.{Enumerable, MapFormats}

import scala.util.Success

class EstablisherDetailsMapSpec extends WordSpecLike with MustMatchers with MapFormats with Enumerable.Implicits {

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear - 20

  "establisherDetailsMap writes" must {
    "write correctly formatted JSON" in {
      val testMap: EstablisherDetailsMap = EstablisherDetailsMap(Map(1 ->
        EstablisherDetails("testFirstName1", "testLastName1", new LocalDate(year, month, day)), 2 ->
        EstablisherDetails("testFirstName2", "testLastName2", new LocalDate(year, month, day))))

      val expectedData = Json.obj(
        "1" -> Json.obj("firstName" -> "testFirstName1", "lastName" -> "testLastName1", "date" -> s"$year-0$month-0$day"),
        "2" -> Json.obj("firstName" -> "testFirstName2", "lastName" -> "testLastName2", "date" -> s"$year-0$month-0$day")
      )
      val result = Json.toJson[EstablisherDetailsMap](testMap)
      result mustEqual expectedData
    }
  }

  "addressYearsMap read" must {
    "read successfully EstablisherDetailsMap" in {

      val expectedData: EstablisherDetailsMap = EstablisherDetailsMap(Map(1 ->
        EstablisherDetails("testFirstName1", "testLastName1", new LocalDate(year, month, day)), 2 ->
        EstablisherDetails("testFirstName2", "testLastName2", new LocalDate(year, month, day))))

      val testJson = Json.obj(
        "1" -> Json.obj("firstName" -> "testFirstName1", "lastName" -> "testLastName1", "date" -> s"$year-0$month-0$day"),
        "2" -> Json.obj("firstName" -> "testFirstName2", "lastName" -> "testLastName2", "date" -> s"$year-0$month-0$day")
      )
      val result = Json.fromJson[EstablisherDetailsMap](testJson).get
      result mustEqual expectedData
    }
  }

  "get" must {
    "return the appropriate Address Years for the given index" in {
      val testMap: EstablisherDetailsMap = EstablisherDetailsMap(Map(1 ->
        EstablisherDetails("testFirstName1", "testLastName1", new LocalDate(year, month, day)), 2 ->
        EstablisherDetails("testFirstName2", "testLastName2", new LocalDate(year, month, day))))

      val establisherDetails = EstablisherDetails("testFirstName1", "testLastName1", new LocalDate(year, month, day))

      testMap.get(1) mustEqual Success(Some(establisherDetails))
    }

    "return None for the valid index but don't have relevant data" in {
      val testMap: EstablisherDetailsMap = EstablisherDetailsMap(Map(1 ->
        EstablisherDetails("testFirstName1", "testLastName1", new LocalDate(year, month, day)), 2 ->
        EstablisherDetails("testFirstName2", "testLastName2", new LocalDate(year, month, day))))

      testMap.get(3) mustEqual Success(None)
    }

    "return failure if the index is invalid" in {
      val testMap: EstablisherDetailsMap = EstablisherDetailsMap(Map(1 ->
        EstablisherDetails("testFirstName1", "testLastName1", new LocalDate(year, month, day)), 2 ->
        EstablisherDetails("testFirstName2", "testLastName2", new LocalDate(year, month, day))))

      testMap.get(11).isFailure mustEqual true
    }
  }
}
