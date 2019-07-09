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

package models.reads

import java.time.LocalDate

import models.person.PersonDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._


class PersonDetailsReadsSpec extends WordSpec with MustMatchers with OptionValues {
  "Person Details json" should {

    "map correctly to person details" when {

      "We have person details with isDeleted defaulted to false when no isDeleted flag is in json" in {
        val payload = Json.obj("firstName" -> "test", "middleName" -> "testVat", "lastName" -> "testPaye", "date" -> LocalDate.now())
        val result = payload.as[PersonDetails]
        result.isDeleted mustBe false
      }

      "We have person details with isDeleted flag to true when isDeleted is present in json" in {
        val payload = Json.obj("firstName" -> "test", "middleName" -> "testVat", "lastName" -> "testPaye", "date" -> LocalDate.now(), "isDeleted" -> true)
        val result = payload.as[PersonDetails]
        result.isDeleted mustBe true
      }
    }
  }
}
