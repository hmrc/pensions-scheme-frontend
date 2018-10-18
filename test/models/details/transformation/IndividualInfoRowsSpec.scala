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

package models.details.transformation

import models.details._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.FakeCountryOptions
import viewmodels.{AnswerRow, AnswerSection, SuperSection}

import scala.language.implicitConversions

class IndividualInfoRowsSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val individualInfoRows: IndividualInfoRows[IndividualInfo] = IndividualInfoRows[IndividualInfo](FakeCountryOptions())

  "IndividualInfoRows" must {

    "produce row of answers" when {

      "all data present" in {

        individualInfoRows.transformRows(individuals) must equal(indidualAnswerRows)
      }

      "nino, utr and previous address are not present" in {

        individualInfoRows.transformRows(individuals.copy(nino = None, utr=None,
          previousAddress = PreviousAddressInfo(false, None))) must equal(Seq(
          AnswerRow("messages__psaSchemeDetails__individual_date_of_birth", Seq("29 March 1955"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__individual_address", Seq(
            "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__individual_less_than_12months", Seq("companyAddressYears.over_a_year"), answerIsMessageKey = true, None),
          AnswerRow("messages__psaSchemeDetails__individual_email", Seq("test@test.com"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__individual_phone", Seq("07592113"), answerIsMessageKey = false, None)
        ))
      }
    }

    "produce correct answer section" when {

      "all correct data is present" in {

        individualInfoRows.transformAnswerSection(individuals) must equal(
          AnswerSection(Some("fName mName lName"), indidualAnswerRows)
        )
      }
    }

    "produce correct super section" when {

      "all correct data is present" in {

        individualInfoRows.transformSuperSection(individuals) must equal(
          SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows)))
        )
      }

      "all correct data is present and custom heading provided" in {

        individualInfoRows.transformSuperSection(individuals, Some("heading")) must equal(
          SuperSection(Some("heading"), Seq(AnswerSection(Some("fName mName lName"), indidualAnswerRows)))
        )
      }
    }

  }
}
