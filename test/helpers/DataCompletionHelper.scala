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

package helpers

import identifiers.register.trustees.individual._
import models._
import models.address.Address
import models.person.PersonName
import models.register.SchemeType
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import org.scalatest.OptionValues
import play.api.libs.json.JsResult
import utils.UserAnswers

import java.time.LocalDate

trait DataCompletionHelper extends OptionValues {
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val stringValue = "value"

  protected def setTrusteeCompletionStatusIndividualDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultIndividualDetails(isComplete, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusAddressDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultAddressDetails(isComplete, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusContactDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultContactDetails(isComplete, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusJsResultIndividualDetails(isComplete: Boolean,
                                                                    index: Int = 0,
                                                                    ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(TrusteeDOBId(index))(LocalDate.now())
        .asOpt
        .value
        .set(TrusteeHasNINOId(index))(true)
        .asOpt
        .value
        .set(TrusteeEnterNINOId(index))(ReferenceValue(stringValue))
        .asOpt
        .value
        .set(TrusteeHasUTRId(index))(true)
        .asOpt
        .value
        .set(TrusteeUTRId(index))(ReferenceValue(stringValue))
    }
    else {
      ua.set(TrusteeDOBId(index))(LocalDate.now())
        .asOpt
        .value
        .set(TrusteeHasNINOId(index))(true)
        .asOpt
        .value
        .set(TrusteeHasUTRId(index))(true)
        .asOpt
        .value
        .set(TrusteeUTRId(index))(ReferenceValue(stringValue))
    }

  protected def setTrusteeCompletionStatusJsResultAddressDetails(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(TrusteeAddressId(index))(address)
        .asOpt
        .value
        .set(TrusteeAddressYearsId(index))(AddressYears.OverAYear)
    }
    else {
      ua.set(TrusteeAddressId(index))(address)
    }

  protected def setTrusteeCompletionStatusJsResultContactDetails(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(TrusteeEmailId(index))(stringValue)
        .asOpt
        .value
        .set(TrusteePhoneId(index))(stringValue)
    }
    else {
      ua.set(TrusteePhoneId(index))(stringValue)
    }

  protected def setTrusteeCompletionStatusJsResult(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    setTrusteeCompletionStatusJsResultContactDetails(
      isComplete,
      index,
      setTrusteeCompletionStatusJsResultAddressDetails(isComplete,
        index,
        setTrusteeCompletionStatusJsResultIndividualDetails(isComplete, index, ua).asOpt.value).asOpt.value
    )

  protected def setTrusteeCompletionStatus(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResult(isComplete, index, ua).asOpt.value

  protected def setCompleteBeforeYouStart(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if (isComplete) {
      ua.schemeName(schemeName = "Test Scheme").
        schemeType(SchemeType.SingleTrust).establishedCountry(country = "GB").
        declarationDuties(haveWorkingKnowledge = true)
    } else {
      ua.schemeName(schemeName = "Test Scheme")
    }
  }

  protected def setCompleteMembers(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if (isComplete) ua.currentMembers(Members.One).futureMembers(Members.One) else ua.currentMembers(Members.One)
  }

  protected def setCompleteBank(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if (isComplete) ua.ukBankAccount(ukBankAccount = false) else ua.ukBankAccount(ukBankAccount = true)
  }

  protected def setCompleteBenefits(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if (isComplete) {
      ua.occupationalPensionScheme(isOccupational = true).
        investmentRegulated(isInvestmentRegulated = true).typeOfBenefits(TypeOfBenefits.MoneyPurchase).
        moneyPurchaseBenefits(MoneyPurchaseBenefits.Collective).benefitsSecuredByInsurance(isInsured = false)
    } else {
      ua.occupationalPensionScheme(isOccupational = true)
        .investmentRegulated(isInvestmentRegulated = true)
        .typeOfBenefits(TypeOfBenefits.MoneyPurchase)
    }
  }

  protected def setCompleteEstIndividual(index: Int, ua: UserAnswers): UserAnswers = {
    ua.establisherKind(index, EstablisherKind.Indivdual)
      .establishersIndividualName(index, PersonName("first", "last")).establishersIndividualDOB(index, LocalDate.now().minusYears(20))
      .establishersIndividualNino(index, ReferenceValue("AB100100A")).establishersIndividualUtr(index, ReferenceValue("1111111111")).
      establishersIndividualAddress(index, address).establishersIndividualAddressYears(index, AddressYears.OverAYear)
      .establishersIndividualEmail(index, "s@s.com").establishersIndividualPhone(index, "123")
  }

  protected def setCompleteTrusteeIndividual(index: Int, ua: UserAnswers): UserAnswers = {
    ua.trusteeKind(index, TrusteeKind.Individual)
      .trusteeName(index, PersonName("first", "last")).trusteeIndividualDOB(index, LocalDate.now().minusYears(20))
      .trusteeIndividualNino(index, ReferenceValue("AB100100A")).trusteeIndividualUtr(index, ReferenceValue("1111111111")).
      trusteesAddress(index, address).trusteesIndividualAddressYears(index, AddressYears.OverAYear)
      .trusteeEmail(index, "s@s.com").trusteePhone(index, "123")
  }

  protected def setCompleteEstCompany(index: Int, ua: UserAnswers): UserAnswers = {
    ua.establisherKind(index, EstablisherKind.Company)
      .establisherCompanyDetails(index, CompanyDetails("test company"))
      .establisherCompanyNoCrnReason(index, "no Crn")
      .establisherCompanyNoUtrReason(index, "no utr")
      .establisherCompanyhasVat(index, false)
      .establisherCompanyhasPaye(index, false)
      .establishersCompanyAddress(index, address)
      .establisherCompanyAddressYears(index, AddressYears.OverAYear)
      .establishersCompanyEmail(index, "s@s.com")
      .establishersCompanyPhone(index, "123")
      .establishersCompanyDirectorName(index, 0, PersonName("dir", "One"))
      .establishersCompanyDirectorDOB(index, 0, LocalDate.now().minusYears(30))
      .establishersCompanyDirectorNino(index, 0, ReferenceValue("AB100100A"))
      .establishersCompanyDirectorUtr(index, 0, ReferenceValue("123"))
      .establishersCompanyDirectorAddress(index, 0, address)
      .establishersCompanyDirectorAddressYears(index, 0, AddressYears.OverAYear)
      .establishersCompanyDirectorEmail(index, 0, "s@s.com")
      .establishersCompanyDirectorPhone(index, 0, "123")
  }


  protected def setCompleteTrusteeCompany(index: Int, ua: UserAnswers): UserAnswers = {
    ua.trusteeKind(index, TrusteeKind.Company)
      .trusteesCompanyDetails(index, CompanyDetails("test company"))
      .trusteesCompanyHasCRN(index, true)
      .trusteesCompanyEnterCRN(index, ReferenceValue("test-crn"))
      .trusteesCompanyHasUTR(index, true)
      .trusteesCompanyEnterUTR(index, ReferenceValue("test-utr"))
      .trusteesCompanyHasVAT(index, true)
      .trusteesCompanyEnterVAT(index, ReferenceValue("test-vat"))
      .trusteesCompanyHasPAYE(index, true)
      .trusteesCompanyPAYE(index, ReferenceValue("test-paye"))
      .trusteesCompanyAddress(index, address)
      .trusteesCompanyAddressYears(index, AddressYears.OverAYear)
      .trusteeCompanyEmail(index, "s@s.com")
      .trusteeCompanyPhone(index, "123")
  }

  protected def setCompleteTrusteePartnership(index: Int, ua: UserAnswers): UserAnswers = {
    ua.trusteeKind(index, TrusteeKind.Partnership)
      .trusteePartnershipDetails(index, PartnershipDetails("test partnership"))
      .trusteesPartnershipHasUTR(index, true)
      .trusteesPartnershipEnterUTR(index, ReferenceValue("test-utr"))
      .trusteesPartnershipHasVAT(index, true)
      .trusteesPartnershipEnterVAT(index, ReferenceValue("test-vat"))
      .trusteesPartnershipHasPAYE(index, true)
      .trusteesPartnershipPAYE(index, ReferenceValue("test-paye"))
      .trusteePartnershipAddress(index, address)
      .trusteePartnershipAddressYears(index, AddressYears.OverAYear)
      .trusteePartnershipEmail(index, "s@s.com")
      .trusteePartnershipPhone(index, "123")
  }

  protected def setCompleteEstPartnershipOnePartner(index: Int, ua: UserAnswers): UserAnswers = {
    ua.establisherKind(index, EstablisherKind.Partnership)
      .establisherPartnershipDetails(index, PartnershipDetails("test partnership"))
      .establisherPartnershipNoUtrReason(index, "no utr")
      .establisherPartnershipHasVat(index, false)
      .establisherPartnershiphasPaye(index, false)
      .establisherPartnershipAddress(index, address)
      .establisherPartnershipAddressYears(index, AddressYears.OverAYear)
      .establishersPartnershipEmail(index, "s@s.com")
      .establishersPartnershipPhone(index, "123")
      .establishersPartnershipPartnerName(index, 0, PersonName("Partner", "One"))
      .establishersPartnershipPartnerDOB(index, 0, LocalDate.now().minusYears(30))
      .establishersPartnershipPartnerNino(index, 0, ReferenceValue("AB100100A"))
      .establishersPartnershipPartnerUtr(index, 0, ReferenceValue("123"))
      .establishersPartnershipPartnerAddress(index, 0, address)
      .establishersPartnershipPartnerAddressYears(index, 0, AddressYears.OverAYear)
      .establishersPartnershipPartnerEmail(index, 0, "s@s.com")
      .establishersPartnershipPartnerPhone(index, 0, "123")
  }

  protected def setCompleteEstPartnership(index: Int, ua: UserAnswers): UserAnswers = {
    setCompleteEstPartnershipOnePartner(index, ua)
      .establishersPartnershipPartnerName(index, 1, PersonName("Partner", "Two"))
      .establishersPartnershipPartnerDOB(index, 1, LocalDate.now().minusYears(30))
      .establishersPartnershipPartnerNino(index, 1, ReferenceValue("AB100100A"))
      .establishersPartnershipPartnerUtr(index, 1, ReferenceValue("123"))
      .establishersPartnershipPartnerAddress(index, 1, address)
      .establishersPartnershipPartnerAddressYears(index, 1, AddressYears.OverAYear)
      .establishersPartnershipPartnerEmail(index, 1, "s@s.com")
      .establishersPartnershipPartnerPhone(index, 1, "123")
  }

  protected def setCompleteWorkingKnowledge(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if (isComplete) {
      ua.adviserName(name = "test adviser").adviserEmailAddress(email = "s@s.com").
        adviserPhone("123").advisersAddress(Address("a", "b", None, None, None, "GB"))
    } else {
      ua.adviserName(name = "test adviser")
    }
  }

  implicit class UserAnswerOps(answers: UserAnswers) {
    def establisherCompanyEntity(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.establisherCompanyDetails(index, CompanyDetails(s"test company $index", isDeleted)).
        isEstablisherNew(index, flag = true).
        establisherKind(index, EstablisherKind.Company)
    }

    def establisherIndividualEntity(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.establishersIndividualName(index, PersonName(s"first $index", s"last $index", isDeleted)).
        isEstablisherNew(index, flag = true).isEstablisherNew(index, flag = true)
        .establisherKind(index, EstablisherKind.Indivdual)
    }

    def establisherPartnershipEntity(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.establisherPartnershipDetails(index, PartnershipDetails(s"test partnership $index", isDeleted)).
        isEstablisherNew(index, flag = true).isEstablisherNew(index, flag = true)
        .establisherKind(index, EstablisherKind.Partnership)
    }

    def trusteeCompanyEntity(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.trusteesCompanyDetails(index, CompanyDetails(s"test company $index", isDeleted)).
        isTrusteeNew(index, flag = true).
        trusteeKind(index, TrusteeKind.Company)
    }

    def trusteeIndividualEntity(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.trusteeName(index, PersonName(s"first $index", s"last $index", isDeleted)).
        isTrusteeNew(index, flag = true).trusteeKind(index, TrusteeKind.Individual)
    }

    def trusteePartnershipEntity(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.trusteePartnershipDetails(index, PartnershipDetails(s"test partnership $index", isDeleted)).
        isTrusteeNew(index, flag = true).trusteeKind(index, TrusteeKind.Partnership)
    }
  }
}
