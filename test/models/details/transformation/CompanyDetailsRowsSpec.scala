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

class CompanyDetailsRowsSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val companyDetailsRows: CompanyDetailsRows[CompanyDetails] = new CompanyDetailsRows[CompanyDetails](FakeCountryOptions())

  "CompanyDetailsRows" must {

    "produce row of answers" when {

      "all data present" in {

        companyDetailsRows.transformRows(companyDetails) must equal(companyAnswerRows)
      }

      "vat, paye, utr, crn and previous address are not present" in {

        companyDetailsRows.transformRows(companyDetails.copy(vatRegistration = None, payeRef= None, crn = None,
          utr=None, previousAddress = None)) must equal(Seq(
          AnswerRow("messages__psaSchemeDetails__company_address", Seq(
            "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__company_email", Seq("test@test.com"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__company_phone", Seq("07592113"), answerIsMessageKey = false, None)
        ))
      }
    }

    "produce correct super section" when {

      "all correct data is present" in {

        companyDetailsRows.transformSuperSection(companyDetails) must equal(
          SuperSection(Some(companyDetails.organizationName), Seq(AnswerSection(None, companyAnswerRows)))
        )
      }

    }
  }
}
