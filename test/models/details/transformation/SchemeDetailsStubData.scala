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

  val partnershipDetails = PartnershipDetails("abc partnership", Some("7897700000"), Some("789770000"), Some("9999"),
    correspondenceAddressDetails,  correspondenceContactDetails, previousAddressDetails, Seq(individuals))

  val establisherDetails = EstablisherInfo(Seq(individuals), Seq(companyDetails), Seq(partnershipDetails))

  val trusteePartnershipDetails = PartnershipDetails("abc partnership", Some("7897700000"), Some("789770000"), Some("9999"),
    correspondenceAddressDetails,  correspondenceContactDetails, previousAddressDetails, Nil)

  val trusteeDetails = TrusteeInfo(Seq(individuals), Seq(companyDetails), Seq(trusteePartnershipDetails))

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
}
