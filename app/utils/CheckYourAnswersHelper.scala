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

package utils

import controllers.register.routes
import identifiers.register._
import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.{CompanyAddressYearsId, CompanyContactDetailsId, CompanyDetailsId}
import identifiers.register.establishers.individual._
import models.register.establishers.individual.{AddressYears, UniqueTaxReference}
import models.{CheckMode, EstablisherNino, Index}
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers) extends Enumerable.Implicits {

  def companyAddressYears(index: Int): Option[AnswerRow] =
    userAnswers.get(CompanyAddressYearsId(index)) match {
      case Some(x) => Some(AnswerRow("companyAddressYears.checkYourAnswersLabel", s"companyAddressYears.$x", true,
        controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode, Index(index)).url))
      case _ => None
    }

  def companyDetails(index: Int): Option[AnswerRow] =
    userAnswers.get(CompanyDetailsId(index)) match {
      case Some(x) => Some(AnswerRow("companyDetails.checkYourAnswersLabel", s"${x.companyName} ${x.vatNumber} ${x.payeNumber}", false,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))
      case _ => None
    }

  def companyContactDetails(index: Int): Option[AnswerRow] = userAnswers.get(CompanyContactDetailsId(index)) match {
    case Some(x) => Some(AnswerRow("companyContactDetails.checkYourAnswersLabel", s"${x.emailAddress} ${x.phoneNumber}", false,
      controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def uniqueTaxReference(index: Int): Option[AnswerRow] =
    userAnswers.get(UniqueTaxReferenceId(index)) match {
    case Some(x) => Some(AnswerRow("uniqueTaxReference.checkYourAnswersLabel", s"${UniqueTaxReference.Yes} ${UniqueTaxReference.No}", false,
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def establisherNino(index: Int): Option[AnswerRow] = userAnswers.get(EstablisherNinoId(index)) match {
    case Some(x) => Some(AnswerRow("uniqueTaxReference.checkYourAnswersLabel", s"${EstablisherNino.Yes} ${EstablisherNino.No}", false,
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def contactDetails(index: Int): Option[AnswerRow] = userAnswers.get(ContactDetailsId(index)) match {
    case Some(x) => Some(AnswerRow("contactDetails.checkYourAnswersLabel", s"${x.emailAddress} ${x.phoneNumber}", false,
      controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def establisherDetails(index: Int): Option[AnswerRow] = userAnswers.get(EstablisherDetailsId(index)) match {
    case Some(x) => Some(AnswerRow("establisherDetails.checkYourAnswersLabel", s"${x.firstName} ${x.lastName}", false,
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def establisherKind(index:Int): Option[AnswerRow] = userAnswers.get(EstablisherKindId(index)) match {
    case Some(x) => Some(AnswerRow("establisherKind.checkYourAnswersLabel",s"${x.toString}",false,
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def schemeEstablishedCountry: Option[AnswerRow] = userAnswers.get(SchemeEstablishedCountryId) map {
    x => AnswerRow("schemeEstablishedCountry.checkYourAnswersLabel", s"$x", false,
      routes.SchemeEstablishedCountryController.onPageLoad(CheckMode).url)
  }

  def uKBankAccount: Option[AnswerRow] = userAnswers.get(UKBankAccountId) map {
    x => AnswerRow("uKBankAccount.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.UKBankAccountController.onPageLoad(CheckMode).url)
  }

  def uKBankDetails: Option[AnswerRow] = userAnswers.get(UKBankDetailsId) map {
    x => AnswerRow("uKBankDetails.checkYourAnswersLabel", s"${x.accountName} ${x.bankName}", false,
      routes.UKBankDetailsController.onPageLoad(CheckMode).url)
  }

  def addressYears(index: Int): Option[AnswerRow] = {
    userAnswers.get[AddressYears](AddressYearsId(index)) match {
      case Some(x) => Some(AnswerRow("addressYears.checkYourAnswersLabel", s"addressYears.$x", true,
        controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, Index(index)).url))
      case _ => None
    }
  }

  def benefits: Option[AnswerRow] = userAnswers.get(BenefitsId) map {
    x => AnswerRow("benefits.checkYourAnswersLabel", s"benefits.$x", true,
      routes.BenefitsController.onPageLoad(CheckMode).url)
  }

  def benefitsInsurer: Option[AnswerRow] = userAnswers.get(BenefitsInsurerId) map {
    x => AnswerRow("benefitsInsurer.checkYourAnswersLabel", s"${x.companyName} ${x.policyNumber}", false,
      routes.BenefitsInsurerController.onPageLoad(CheckMode).url)
  }

  def membership: Option[AnswerRow] = userAnswers.get(MembershipId) map {
    x => AnswerRow("membership.checkYourAnswersLabel", s"membership.$x", true,
      routes.MembershipController.onPageLoad(CheckMode).url)
  }

  def membershipFuture: Option[AnswerRow] = userAnswers.get(MembershipFutureId) map {
    x => AnswerRow("membershipFuture.checkYourAnswersLabel", s"membershipFuture.$x", true,
      routes.MembershipFutureController.onPageLoad(CheckMode).url)
  }

  def investmentRegulated: Option[AnswerRow] = userAnswers.get(InvestmentRegulatedId) map {
    x => AnswerRow("investmentRegulated.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.InvestmentRegulatedController.onPageLoad(CheckMode).url)
  }

  def securedBenefits: Option[AnswerRow] = userAnswers.get(SecuredBenefitsId) map {
    x => AnswerRow("securedBenefits.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.SecuredBenefitsController.onPageLoad(CheckMode).url)
  }

  def occupationalPensionScheme: Option[AnswerRow] = userAnswers.get(OccupationalPensionSchemeId) map {
    x => AnswerRow("occupationalPensionScheme.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url)
  }

  def schemeDetails: Option[AnswerRow] = userAnswers.get(SchemeDetailsId) map {
    x => AnswerRow("schemeDetails.checkYourAnswersLabel", s"${x.schemeName} ${x.schemeType}", false,
      routes.SchemeDetailsController.onPageLoad(CheckMode).url)
  }
}
