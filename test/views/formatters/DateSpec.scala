/*
 * Copyright 2020 HM Revenue & Customs
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

package views.formatters

import java.time.format.DateTimeParseException

import org.scalatest.{MustMatchers, WordSpec}

class DateSpec extends WordSpec with MustMatchers {

  "The date formatter" must {
    "display the date in the format e.g. 29 November 2017" in {
      Date.IsoLocalToViewDate(validDate) mustBe "10 August 2012"
    }

    "display the correct date for the first day of the year" in {
      Date.IsoLocalToViewDate(startOfYear) mustBe "1 January 2000"
    }

    "display the correct date for the last day of the year" in {
      Date.IsoLocalToViewDate(endOfYear) mustBe "31 December 2000"
    }

    "throw an error" when {
      "given a string with a date in an incorrect format" in {
        an[DateTimeParseException] should be thrownBy Date.IsoLocalToViewDate(invalidDate)
      }

      "given a string that is not a date" in {
        an[DateTimeParseException] should be thrownBy Date.IsoLocalToViewDate(notADate)
      }
    }
  }

  private val validDate = "2012-08-10"
  private val startOfYear = "2000-01-01"
  private val endOfYear = "2000-12-31"
  private val invalidDate = "21 July 2012"
  private val notADate = "not a date"
}

