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

import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import identifiers._
import identifiers.register._
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company.{CompanyEnterPAYEId, CompanyEnterCRNId, CompanyEnterUTRId}
import identifiers.register.trustees.individual.TrusteeNameId
import models._
import models.address.{Address, TolerantAddress}
import models.person.PersonName
import models.register.{establishers => _, trustees => _, _}
import org.scalatest.OptionValues
import play.api.i18n.Messages

//scalastyle:off number.of.methods
package object utils {

  implicit class UserAnswerOps(answers: UserAnswers) extends OptionValues with Enumerable .Implicits {

    def occupationalPensionScheme(isOccupational: Boolean): UserAnswers = {
      answers.set(identifiers.OccupationalPensionSchemeId)(isOccupational).asOpt.value
    }

    def typeOfBenefits(benefits: TypeOfBenefits): UserAnswers = {
      answers.set(identifiers.TypeOfBenefitsId)(benefits).asOpt.value
    }

    def benefitsSecuredByInsurance(isInsured: Boolean): UserAnswers = {
      answers.set(identifiers.BenefitsSecuredByInsuranceId)(isInsured).asOpt.value
    }

    def schemeName(schemeName: String): UserAnswers = {
      answers.set(SchemeNameId)(schemeName).asOpt.value
    }

    def insuranceCompanyName(companyName: String): UserAnswers = {
      answers.set(InsuranceCompanyNameId)(companyName).asOpt.value
    }

    def insurancePolicyNumber(policyNumber: String): UserAnswers = {
      answers.set(InsurancePolicyNumberId)(policyNumber).asOpt.value
    }

    def establishedCountry(country: String): UserAnswers = {
      answers.set(EstablishedCountryId)(country).asOpt.value
    }

    def schemeUkBankAccount(haveUkBankAccount: Boolean): UserAnswers = {
      answers.set(identifiers.UKBankAccountId)(haveUkBankAccount).asOpt.value
    }

    def bankAccountDetails(bankAccountDetails: BankAccountDetails): UserAnswers = {
      answers.set(identifiers.BankAccountDetailsId)(bankAccountDetails).asOpt.value
    }

    def currentMembers(currentMembers: Members): UserAnswers = {
      answers.set(CurrentMembersId)(currentMembers).asOpt.value
    }

    def futureMembers(futureMembers: Members): UserAnswers = {
      answers.set(FutureMembersId)(futureMembers).asOpt.value
    }

    def investmentRegulated(isInvestmentRegulated: Boolean): UserAnswers = {
      answers.set(InvestmentRegulatedSchemeId)(isInvestmentRegulated).asOpt.value
    }

    def schemeType(schemeType: SchemeType): UserAnswers = {
      answers.set(SchemeTypeId)(schemeType).asOpt.value
    }

    def insurerConfirmAddress(address: Address): UserAnswers = {
      answers.set(identifiers.InsurerConfirmAddressId)(address).asOpt.value
    }

    def insurerSelectAddress(address: TolerantAddress): UserAnswers = {
      answers.set(identifiers.InsurerSelectAddressId)(address).asOpt.value
    }

    def ukBankAccount(ukBankAccount: Boolean): UserAnswers = {
      answers.set(identifiers.UKBankAccountId)(ukBankAccount).asOpt.value
    }

    //Establishers Individual

    def establishersIndividualAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.individual.AddressId(index))(address).asOpt.value
    }

    def establishersIndividualAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(establishers.individual.AddressYearsId(index))(addressYears).asOpt.value
    }

    def trusteesIndividualAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(trustees.individual.TrusteeAddressYearsId(index))(addressYears).asOpt.value
    }

    def establishersIndividualAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(establishers.individual.AddressListId(index))(selectedAddress).asOpt.value
    }

    def establishersIndividualPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.individual.PreviousAddressId(index))(address).asOpt.value
    }

    def establishersIndividualPreviousAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(establishers.individual.PreviousAddressListId(index))(selectedAddress).asOpt.value
    }

    def establishersIndividualName(index: Int, name: PersonName): UserAnswers = {
      answers.set(establishers.individual.EstablisherNameId(index))(name).asOpt.value
    }

    def establishersIndividualEmail(index: Int, email: String): UserAnswers = {
      answers.set(establishers.individual.EstablisherEmailId(index))(email).asOpt.value
    }

    def establishersIndividualPhone(index: Int, phone: String): UserAnswers = {
      answers.set(establishers.individual.EstablisherPhoneId(index))(phone).asOpt.value
    }

    // Establishers company
    def establisherCompanyDetails(index: Int, companyDetails: CompanyDetails): UserAnswers = {
      answers.set(establishers.company.CompanyDetailsId(index))(companyDetails).asOpt.value
    }

    def establisherCompanyDormant(index: Int, isDormant: DeclarationDormant): UserAnswers = {
      answers.set(establishers.company.IsCompanyDormantId(index))(isDormant).asOpt.value
    }

    def establisherCompanyAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(establishers.company.CompanyAddressYearsId(index))(addressYears).asOpt.value
    }

    def establisherCompanyTradingTime(index: Int, hasBeenTrading: Boolean): UserAnswers = {
      answers.set(establishers.company.HasBeenTradingCompanyId(index))(hasBeenTrading).asOpt.value
    }

    def establishersCompanyAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.company.CompanyAddressId(index))(address).asOpt.value
    }

    def establishersCompanyAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(establishers.company.CompanyAddressListId(index))(selectedAddress).asOpt.value
    }

    def establishersCompanyPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.company.CompanyPreviousAddressId(index))(address).asOpt.value
    }

    def establishersCompanyPreviousAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(establishers.company.CompanyPreviousAddressListId(index))(selectedAddress).asOpt.value
    }

    def isEstablisherNew(index: Int, flag: Boolean): UserAnswers = {
      answers.set(IsEstablisherNewId(index))(flag).asOpt.value
    }

    //Establisher company director
    def establishersCompanyDirectorAddress(establisherId: Int, directorId: Int, address: Address): UserAnswers = {
      answers.set(establishers.company.director.DirectorAddressId(establisherId, directorId))(address).asOpt.value
    }

    def establishersCompanyDirectorAddressList(establisherId: Int, directorId: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(establishers.company.director.DirectorAddressListId(establisherId, directorId))(selectedAddress).asOpt.value
    }

    def establishersCompanyDirectorPreviousAddress(establisherId: Int, directorId: Int, address: Address): UserAnswers = {
      answers.set(establishers.company.director.DirectorPreviousAddressId(establisherId, directorId))(address).asOpt.value
    }

    def establishersCompanyDirectorPreviousAddressList(establisherId: Int, directorId: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(establishers.company.director.DirectorPreviousAddressListId(establisherId, directorId))(selectedAddress).asOpt.value
    }

    // Establishers partnership

    def establisherPartnershipDetails(index: Int, partnershipDetails: PartnershipDetails): UserAnswers = {
      answers.set(establishers.partnership.PartnershipDetailsId(index))(partnershipDetails).asOpt.value
    }

    def establishersPartnershipPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.partnership.PartnershipPreviousAddressId(index))(address).asOpt.value
    }

    def establisherPartnershipAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.partnership.PartnershipAddressId(index))(address).asOpt.value
    }

    def establisherPartnershipAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(establishers.partnership.PartnershipAddressYearsId(index))(addressYears).asOpt.value
    }

    def establisherPartnershipTradingTime(index: Int, hasBeenTrading: Boolean): UserAnswers = {
      answers.set(establishers.partnership.PartnershipHasBeenTradingId(index))(hasBeenTrading).asOpt.value
    }

    // Trustees company
    def trusteesCompanyAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.company.CompanyAddressId(index))(address).asOpt.value
    }

    def trusteesCompanyAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(trustees.company.CompanyAddressListId(index))(address).asOpt.value
    }

    def trusteesCompanyAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(trustees.company.CompanyAddressYearsId(index))(addressYears).asOpt.value
    }

    def trusteesCompanyEnterVAT(index: Int, reference: ReferenceValue): UserAnswers = {
      answers.set(trustees.company.CompanyEnterVATId(index))(reference).asOpt.value
    }

    def trusteesCompanyPayeVariations(index: Int, paye: ReferenceValue): UserAnswers = {
      answers.set(CompanyEnterPAYEId(index))(paye).asOpt.value
    }

    def trusteesCompanyCrnVariations(index: Int, crn: ReferenceValue): UserAnswers = {
      answers.set(CompanyEnterCRNId(index))(crn).asOpt.value
    }

    def trusteesCompanyUtr(index: Int, utr: ReferenceValue): UserAnswers = {
      answers.set(CompanyEnterUTRId(index))(utr).asOpt.value
    }



    def trusteesCompanyDetails(index: Int, companyDetails: CompanyDetails): UserAnswers = {
      answers.set(trustees.company.CompanyDetailsId(index))(companyDetails).asOpt.value
    }

    def trusteesCompanyPhone(index: Int, phone: String): UserAnswers = {
      answers.set(trustees.company.CompanyPhoneId(index))(phone).asOpt.value
    }

    def trusteesCompanyEmail(index: Int, email: String): UserAnswers = {
      answers.set(trustees.company.CompanyEmailId(index))(email).asOpt.value
    }


    def trusteesCompanyPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.company.CompanyPreviousAddressId(index))(address).asOpt.value
    }

    def trusteesCompanyPreviousAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(trustees.company.CompanyPreviousAddressListId(index))(selectedAddress).asOpt.value
    }

    def trusteeCompanyTradingTime(index: Int, hasBeenTrading: Boolean): UserAnswers = {
      answers.set(trustees.company.HasBeenTradingCompanyId(index))(hasBeenTrading).asOpt.value
    }

    def trusteeCompanyEmail(index: Int, email: String): UserAnswers = {
      answers.set(trustees.company.CompanyEmailId(index))(email).asOpt.value
    }

    def trusteeCompanyPhone(index: Int, phone: String): UserAnswers = {
      answers.set(trustees.company.CompanyPhoneId(index))(phone).asOpt.value
    }

    //Trustee Individual

    def trusteeName(index: Int, trusteeName: PersonName): UserAnswers = {
      answers.set(TrusteeNameId(index))(trusteeName).asOpt.value
    }

    def trusteesAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.individual.TrusteeAddressId(index))(address).asOpt.value
    }

    def trusteesAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(trustees.individual.IndividualAddressListId(index))(address).asOpt.value
    }

    def trusteesPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.individual.TrusteePreviousAddressId(index))(address).asOpt.value
    }

    def isTrusteeNew(index: Int, flag: Boolean): UserAnswers = {
      answers.set(IsTrusteeNewId(index))(flag).asOpt.value
    }

    def trusteesPreviousAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(trustees.individual.TrusteePreviousAddressListId(index))(selectedAddress).asOpt.value
    }

    //Trustee Partnership
    def trusteePartnershipDetails(index: Int, partnershipDetails: PartnershipDetails): UserAnswers = {
      answers.set(trustees.partnership.PartnershipDetailsId(index))(partnershipDetails).asOpt.value
    }

    def trusteePartnershipAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.partnership.PartnershipAddressId(index))(address).asOpt.value
    }

    def trusteePartnershipAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(trustees.partnership.PartnershipAddressListId(index))(address).asOpt.value
    }

    def trusteesPartnershipPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.partnership.PartnershipPreviousAddressId(index))(address).asOpt.value
    }

    def trusteePartnershipEmail(index: Int, email: String): UserAnswers = {
      answers.set(trustees.partnership.PartnershipEmailId(index))(email).asOpt.value
    }

    def trusteePartnershipPhone(index: Int, phone: String): UserAnswers = {
      answers.set(trustees.partnership.PartnershipPhoneId(index))(phone).asOpt.value
    }

    def trusteePartnershipAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(trustees.partnership.PartnershipAddressYearsId(index))(addressYears).asOpt.value
    }

    def trusteePartnershipTradingTime(index: Int, hasBeenTrading: Boolean): UserAnswers = {
      answers.set(trustees.partnership.PartnershipHasBeenTradingId(index))(hasBeenTrading).asOpt.value
    }

    //Advisers
    def advisersAddress(address: Address): UserAnswers = {
      answers.set(AdviserAddressId)(address).asOpt.value
    }

    def adviserEmailAddress(email: String): UserAnswers = {
      answers.set(AdviserEmailId)(email).asOpt.value
    }

    def adviserPhone(phone: String): UserAnswers = {
      answers.set(AdviserPhoneNumberId)(phone).asOpt.value
    }

    def workingKnowledgePersonPhone(phone: String): UserAnswers = {
      answers.set(identifiers.AdviserPhoneNumberId)(phone).asOpt.value
    }

    def adviserName(name: String): UserAnswers = {
      answers.set(AdviserNameId)(name).asOpt.value
    }

    def advisersAddressList(selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(AdviserAddressListId)(selectedAddress).asOpt.value
    }

    // Other
    def lastPage(page: LastPage): UserAnswers = {
      answers.set(LastPageId)(page).asOpt.value
    }

    // Converters
    def dataRetrievalAction: DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }

  }
}
