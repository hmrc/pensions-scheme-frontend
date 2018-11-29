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

import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import identifiers.LastPageId
import identifiers.register._
import models._
import models.address.{Address, TolerantAddress}
import models.register.SchemeDetails
import org.scalatest.OptionValues

//scalastyle:off number.of.methods
package object utils {

  implicit class UserAnswerOps(answers: UserAnswers) extends OptionValues {

    // Scheme details
    def declarationDuties(declarationDuties: Boolean): UserAnswers = {
      answers.set(DeclarationDutiesId)(declarationDuties).asOpt.value
    }

    def schemeDetails(schemeDetails: SchemeDetails): UserAnswers = {
      answers.set(SchemeDetailsId)(schemeDetails).asOpt.value
    }

    def securedBenefits(securedBenefits: Boolean): UserAnswers = {
      answers.set(SecuredBenefitsId)(securedBenefits).asOpt.value
    }

    def insurerAddress(address: Address): UserAnswers = {
      answers.set(InsurerAddressId)(address).asOpt.value
    }

    def ukBankAccount(ukBankAccount: Boolean): UserAnswers = {
      answers.set(UKBankAccountId)(ukBankAccount).asOpt.value
    }

    //Establishers Individual
    def establishersIndividualAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.individual.AddressId(index))(address).asOpt.value
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

    // Establishers company
    def establisherCompanyDetails(index: Int, companyDetails: CompanyDetails): UserAnswers = {
      answers.set(establishers.company.CompanyDetailsId(index))(companyDetails).asOpt.value
    }

    def establisherPartnershipDetails(index: Int, partnershipDetails: PartnershipDetails): UserAnswers = {
      answers.set(establishers.partnership.PartnershipDetailsId(index))(partnershipDetails).asOpt.value
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

    def establishersPartnershipPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(establishers.partnership.PartnershipPreviousAddressId(index))(address).asOpt.value
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

    def trusteesCompanyContactDetails(index: Int, contactDetails: ContactDetails): UserAnswers = {
      answers.set(trustees.company.CompanyContactDetailsId(index))(contactDetails).asOpt.value
    }

    def trusteesCompanyDetails(index: Int, companyDetails: CompanyDetails): UserAnswers = {
      answers.set(trustees.company.CompanyDetailsId(index))(companyDetails).asOpt.value
    }

    def trusteesCompanyPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.company.CompanyPreviousAddressId(index))(address).asOpt.value
    }

    def trusteesCompanyPreviousAddressList(index: Int, selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(trustees.company.CompanyPreviousAddressListId(index))(selectedAddress).asOpt.value
    }

    def trusteesCompanyRegistrationNumber(index: Int, crn: CompanyRegistrationNumber): UserAnswers = {
      answers.set(trustees.company.CompanyRegistrationNumberId(index))(crn).asOpt.value
    }

    def trusteesUniqueTaxReference(index: Int, utr: UniqueTaxReference): UserAnswers = {
      answers.set(trustees.company.CompanyUniqueTaxReferenceId(index))(utr).asOpt.value
    }

    //Trustee Individual
    def trusteesAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.individual.TrusteeAddressId(index))(address).asOpt.value
    }

    def trusteesAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(trustees.individual.IndividualAddressListId(index))(address).asOpt.value
    }

    def trusteesPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.individual.TrusteePreviousAddressId(index))(address).asOpt.value
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

    //Advisers
    def advisersAddress(address: Address): UserAnswers = {
      answers.set(adviser.AdviserAddressId)(address).asOpt.value
    }

    def advisersAddressList(selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(adviser.AdviserAddressListId)(selectedAddress).asOpt.value
    }

    //Insurers
    def insurersAddress(address: Address): UserAnswers = {
      answers.set(InsurerAddressId)(address).asOpt.value
    }

    def insurersAddressList(selectedAddress: TolerantAddress): UserAnswers = {
      answers.set(InsurerAddressListId)(selectedAddress).asOpt.value
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
