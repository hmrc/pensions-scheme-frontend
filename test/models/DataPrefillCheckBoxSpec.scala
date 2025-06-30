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

package models

import base.SpecBase
import models.prefill.{IndividualDetails => DataPrefillIndividualDetails}

import java.time.LocalDate

class DataPrefillCheckBoxSpec extends SpecBase {

  "checkboxes" must {
    "where 2 establishers each with 2 directors return each item with a sequential number plus none value" in {
      val values = Seq(
        DataPrefillIndividualDetails(
          firstName = "Test",
          lastName = "User 1",
          isDeleted = false,
          nino = None,
          dob = Some(LocalDate.parse("1999-01-13")),
          index = 0,
          isComplete = true,
          mainIndex = Some(0)
        ),
        DataPrefillIndividualDetails(
          firstName = "Test",
          lastName = "User 2",
          isDeleted = false,
          nino = None,
          dob = Some(LocalDate.parse("1999-01-13")),
          index = 1,
          isComplete = true,
          mainIndex = Some(0)
        ),
        DataPrefillIndividualDetails(
          firstName = "Test",
          lastName = "User 3",
          isDeleted = false,
          nino = None,
          dob = Some(LocalDate.parse("1999-01-13")),
          index = 0,
          isComplete = true,
          mainIndex = Some(1)
        ),
        DataPrefillIndividualDetails(
          firstName = "Test",
          lastName = "User 4",
          isDeleted = false,
          nino = None,
          dob = Some(LocalDate.parse("1999-01-13")),
          index = 1,
          isComplete = true,
          mainIndex = Some(1)
        )
      )
      val result = DataPrefillCheckboxOptions(values)
      result.head.value mustBe "0"
      result(1).value mustBe "1"
      result(2).value mustBe "2"
      result(3).value mustBe "3"
      result(4).value mustBe "-1"
    }

  }
}
