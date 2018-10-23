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
import viewmodels.{AnswerRow, AnswerSection, MasterSection, SuperSection}

trait SchemeDetailsStubData {

  val personalName = IndividualName("fName", Some("mName"), "lName")
  val personalDetails =  PersonalInfo(personalName, "1955-03-29")
  val correspondenceAddressDetails = CorrespondenceAddress("address line 1", "address line 2", Some("address line 3"), None, "GB", Some("AB1 1AB"))
  val correspondenceContactDetails = IndividualContactDetails(telephone="07592113", email = "test@test.com")
  val previousAddressDetails = PreviousAddressInfo(isPreviousAddressLast12Month=true, Some(correspondenceAddressDetails))

  val individuals = IndividualInfo(personalDetails, Some("AA999999A"), Some("1234567892"),
    correspondenceAddressDetails,  correspondenceContactDetails, previousAddressDetails)

  val companyDetails = CompanyDetails("abc organisation", Some("7897700000"), Some("AA999999A"), Some("789770000"), Some("9999"),
    correspondenceAddressDetails,  correspondenceContactDetails, Some(previousAddressDetails), Seq(individuals))

  val trusteeCompanyDetails = CompanyDetails("abc organisation", Some("7897700000"), Some("AA999999A"), Some("789770000"), Some("9999"),
    correspondenceAddressDetails,  correspondenceContactDetails, Some(previousAddressDetails), Nil)

  val partnershipDetails = PartnershipDetails("abc partnership", Some("7897700000"), Some("789770000"), Some("9999"),
    correspondenceAddressDetails,  correspondenceContactDetails, previousAddressDetails, Seq(individuals))

  val establisherDetails = EstablisherInfo(Seq(individuals), Seq(companyDetails), Seq(partnershipDetails))

  val trusteePartnershipDetails = PartnershipDetails("abc partnership", Some("7897700000"), Some("789770000"), Some("9999"),
    correspondenceAddressDetails,  correspondenceContactDetails, previousAddressDetails, Nil)

  val trusteeDetails = TrusteeInfo(Seq(individuals), Seq(trusteeCompanyDetails), Seq(trusteePartnershipDetails))

  val psaDetails1 = PsaDetails("A0000001",Some("org name test"),Some(Name(Some("Mickey"),Some("m"),Some("Mouse"))))
  val psaDetails2 = PsaDetails("1234444444",Some("org name test"),Some(Name(Some("Mickey"),Some("m"),Some("Mouse"))))

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
      address = Some(correspondenceAddressDetails))))

  val minimumSchemeDetails = schemeDetails.copy(typeOfScheme = None, insuranceCompany = None)

  val psaSchemeDetailsSample = PsaSchemeDetails(
    SchemeDetails(
      Some("AAABA932JASDA"),
      Some("A3DCADAA"),
      "Pending",
      "Test Scheme",
      isMasterTrust=true,
      Some("Other"),
      Some("Other type"),
      hasMoreThanTenTrustees=true,
      SchemeMemberNumbers("1","2"),
      isInvestmentRegulated=true,
      isOccupational=true,
      "Defined Benefits only",
      "GB",
      areBenefitsSecured=true,
      Some(InsuranceCompany(
        Some("Test Insurance"),
        Some("ADN3JDA"),
        Some(CorrespondenceAddress(
          "line1","line2",Some("line3"),Some("line4"),"GB",Some("NE1")))))),
      Some(establisherDetails),
      Some(trusteeDetails),
      List(psaDetails1, psaDetails2))

  val indidualAnswerRows = Seq(
    AnswerRow("messages__psaSchemeDetails__individual_date_of_birth", Seq("29 March 1955"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__individual_nino", Seq("AA999999A"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__individual_utr", Seq("1234567892"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__individual_address", Seq(
      "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__individual_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__individual_previous_address", Seq(
      "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__individual_email", Seq("test@test.com"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__individual_phone", Seq("07592113"), answerIsMessageKey = false, None)
  )

  val companyAnswerRows = Seq(
    AnswerRow("messages__psaSchemeDetails__company_vat", Seq("789770000"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_paye", Seq("9999"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_crn", Seq("AA999999A"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_utr", Seq("7897700000"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_address", Seq(
      "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__company_previous_address", Seq(
      "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_email", Seq("test@test.com"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__company_phone", Seq("07592113"), answerIsMessageKey = false, None)
  )

  val partnershipAnswerRows = Seq(
    AnswerRow("messages__psaSchemeDetails__partnership_vat", Seq("789770000"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__partnership_paye", Seq("9999"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__partnership_utr", Seq("7897700000"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__partnership_address", Seq(
      "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__partnership_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__partnership_previous_address", Seq(
      "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__partnership_email", Seq("test@test.com"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__partnership_phone", Seq("07592113"), answerIsMessageKey = false, None)
  )


  val expectedSchemeDetailsRows = Seq(
    AnswerRow("messages__psaSchemeDetails__country_established", Seq("Country of GB"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__current_scheme_members", Seq("1"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__future_scheme_members", Seq("2 to 11"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__is_investment_regulated", Seq("site.no"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__is_occupational", Seq("site.no"), answerIsMessageKey = true, None),
    AnswerRow("messages__psaSchemeDetails__benefits", Seq("Defined benefits only"), answerIsMessageKey = false, None),
    AnswerRow("messages__psaSchemeDetails__are_benefits_secured", Seq("site.no"), answerIsMessageKey = true, None)
  )

  val individualAnswerRow = AnswerSection(Some("fName mName lName"), indidualAnswerRows)

  val individualMasterSection = MasterSection(None,Seq(SuperSection(None, Seq(AnswerSection(None, expectedSchemeDetailsRows)))))

  val establisherMasterSection = MasterSection(Some("messages__psaSchemeDetails__establishers"),
    Seq(
      SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows))),
      SuperSection(Some("abc organisation"), Seq(AnswerSection(None, companyAnswerRows))),
      SuperSection(Some("messages__psaSchemeDetails__director_details"), Seq(individualAnswerRow)),
      SuperSection(Some("abc partnership"), Seq(AnswerSection(None, partnershipAnswerRows))),
      SuperSection(Some("messages__psaSchemeDetails__partner_details"), Seq(individualAnswerRow))
    )
  )

  val trsuteeMasterSection = MasterSection(Some("messages__psaSchemeDetails__trustees"),
    Seq(
      SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows))),
      SuperSection(Some("abc organisation"), Seq(AnswerSection(None, companyAnswerRows))),
      SuperSection(Some("abc partnership"), Seq(AnswerSection(None, partnershipAnswerRows)))
    )
  )
}
