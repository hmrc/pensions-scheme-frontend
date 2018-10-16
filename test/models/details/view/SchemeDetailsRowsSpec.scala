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

package models.details.view

import models.details.{CorrespondenceAddress, InsuranceCompany, SchemeDetails, SchemeMemberNumbers}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.FakeCountryOptions
import viewmodels.AnswerRow

import scala.language.implicitConversions

class SchemeDetailsRowsSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues {

  val correspondenceAddress = CorrespondenceAddress("address line 1", "address line 2", Some("address line 3"), None, "GB", Some("AB1 1AB"))

  val schemeDetails = SchemeDetails(srn = None,
    pstr =None,
    status = "test",
    name = "Test Name",
    isMasterTrust = false,
    typeOfScheme = Some("Single trust"),
    otherTypeOfScheme = None,
    hasMoreThanTenTrustees= false,
    members =  SchemeMemberNumbers(current = "1", future =  "2 to 11"),
    isInvestmentRegulated = false,
    isOccupational = false,
    benefits =  "Defined benefits only",
    country= "GB",
    areBenefitsSecured= false,
    insuranceCompany = Some(InsuranceCompany(name = Some("company name"),policyNumber= Some("123456789"),
      address = Some(correspondenceAddress))))

  val schemeDetailsRows: SchemeDetailsRows[SchemeDetails] = SchemeDetailsRows[SchemeDetails](FakeCountryOptions())

  val excpectedSeq = Seq(
    AnswerRow("messages__psaSchemeDetails__country_established", Seq("Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__current_scheme_members", Seq("1"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__future_scheme_members", Seq("2 to 11"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__is_investment_regulated", Seq("site.no"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__is_occupational", Seq("site.no"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__benefits", Seq("Defined benefits only"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__are_benefits_secured", Seq("site.no"), answerIsMessageKey = true, None)
  )

  "SchemeDetailsRows " must {

    "produce row of answers" when {

      "all data present" in {

        schemeDetailsRows.row(schemeDetails) must equal(
          Seq(
            AnswerRow("messages__psaSchemeDetails__scheme_type", Seq("messages__scheme_details__type_single"), answerIsMessageKey = false, None)
          )++excpectedSeq ++
          Seq(
              AnswerRow("messages__psaSchemeDetails__insurance_company_name", Seq("company name"), answerIsMessageKey = false, None),
              AnswerRow("messages__psaSchemeDetails__policy_number", Seq("123456789"), answerIsMessageKey = false, None),
              AnswerRow("messages__psaSchemeDetails__insurance_company_address",
                Seq("address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None)
          ))
      }

      "scheme type and insuranceCompany are not present" in {

        schemeDetailsRows.row(schemeDetails.copy(typeOfScheme = None, insuranceCompany = None)) must equal(excpectedSeq)
      }
    }
  }
}
