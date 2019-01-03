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

package models.details.transformation

import models.details.SchemeDetails
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.FakeCountryOptions
import viewmodels.{AnswerRow, AnswerSection, SuperSection}

import scala.language.implicitConversions

class SchemeDetailsSectionSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData{

  val schemeDetailsRows: SchemeDetailsSection[SchemeDetails] = new SchemeDetailsSection[SchemeDetails](FakeCountryOptions())

  "SchemeDetailsRows" must {

    "produce details section" when{
      "called with correct details" in {
        schemeDetailsRows.transformSuperSection(minimumSchemeDetails) must equal(
          SuperSection(None, Seq(AnswerSection(None, expectedSchemeDetailsRows)))
        )
      }
    }

    "produce row of answers" when {

      "all data present" in {

        schemeDetailsRows.transformRows(schemeDetails) must equal(
          Seq(
            AnswerRow("messages__psaSchemeDetails__scheme_type", Seq("messages__scheme_details__type_single"), answerIsMessageKey = true, None)
          )++expectedSchemeDetailsRows ++
          Seq(
              AnswerRow("messages__psaSchemeDetails__insurance_company_name", Seq("company name"), answerIsMessageKey = false, None),
              AnswerRow("messages__psaSchemeDetails__policy_number", Seq("123456789"), answerIsMessageKey = false, None),
              AnswerRow("messages__psaSchemeDetails__insurance_company_address",
                Seq("address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None)
          ))
      }

      "scheme type and insuranceCompany are not present" in {

        schemeDetailsRows.transformRows(minimumSchemeDetails) must equal(expectedSchemeDetailsRows)
      }
    }
  }
}
