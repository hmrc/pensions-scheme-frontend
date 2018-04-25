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
import identifiers.register._
import models._
import models.address.Address
import models.register.SchemeDetails
import org.scalatest.OptionValues

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

    def ukBankAccount(ukBankAccount: Boolean): UserAnswers = {
      answers.set(UKBankAccountId)(ukBankAccount).asOpt.value
    }

    // Establishers company
    def establisherCompanyDetails(index: Int, companyDetails: CompanyDetails): UserAnswers = {
      answers.set(establishers.company.CompanyDetailsId(index))(companyDetails).asOpt.value
    }

    def trusteesCompanyAddress(index: Int, address: Address): UserAnswers = {
      answers.set(trustees.company.CompanyAddressId(index))(address).asOpt.value
    }

    // Trustees company
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

    def trusteesCompanyRegistrationNumber(index: Int, crn: CompanyRegistrationNumber): UserAnswers = {
      answers.set(trustees.company.CompanyRegistrationNumberId(index))(crn).asOpt.value
    }

    def trusteesUniqueTaxReference(index: Int, utr: UniqueTaxReference): UserAnswers = {
      answers.set(trustees.company.CompanyUniqueTaxReferenceId(index))(utr).asOpt.value
    }

    // Converters
    def dataRetrievalAction: DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }

  }

}
