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
import identifiers.register.establishers.company._
import identifiers.register.establishers.individual._
import models.EstablisherNino.{No, Yes}
import models.addresslookup.Address
import models.register.CountryOptions
import models.register.establishers.individual.{AddressYears, UniqueTaxReference}
import models.{CheckMode, EstablisherNino, Index}
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) extends Enumerable.Implicits {

  def companyAddressYears(index: Int): Option[AnswerRow] =
    userAnswers.get(CompanyAddressYearsId(index)) match {
      case Some(x) => Some(AnswerRow("companyAddressYears.checkYourAnswersLabel", Seq(s"companyAddressYears.$x"), true,
        controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode, Index(index)).url))
      case _ => None
    }

  def companyUniqueTaxReference(index: Int): Option[AnswerRow] =
    userAnswers.get(CompanyUniqueTaxReferenceId(index)) match {
    case Some(_) => Some(AnswerRow("companyUniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.Yes} ${UniqueTaxReference.No}"), false,
      controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def companyRegistrationNumber(index:Int): Option[AnswerRow] =
    userAnswers.get(CompanyRegistrationNumberId(index)) match {
      case Some(x) => Some(AnswerRow("companyRegistrationNumber.checkYourAnswersLabel", Seq(s"companyRegistrationNumber.$x"), true,
        controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode, Index(index)).url))
      case _=> None
  }

  def companyDetails(index: Int): Option[AnswerRow] =
    userAnswers.get(CompanyDetailsId(index)) match {
      case Some(x) => Some(AnswerRow("companyDetails.checkYourAnswersLabel", Seq(s"${x.companyName} ${x.vatNumber} ${x.payeNumber}"), false,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))
      case _ => None
    }

  def companyContactDetails(index: Int): Option[AnswerRow] = userAnswers.get(CompanyContactDetailsId(index)) match {
    case Some(x) => Some(AnswerRow("companyContactDetails.checkYourAnswersLabel", Seq(s"${x.emailAddress} ${x.phoneNumber}"), false,
      controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def address(index: Int): Seq[AnswerRow] = userAnswers.get(AddressId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("address.checkYourAnswersLabel", addressAnswer(x), false,
          controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def previousAddress(index: Int): Seq[AnswerRow] = userAnswers.get(PreviousAddressId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("previousAddress.checkYourAnswersLabel", addressAnswer(x), false,
          controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def addressAnswer(x: Address): Seq[String] = {
    val country = countryOptions.options.find(_.value == x.country).map(_.label).getOrElse(x.country)
    Seq(Some(s"${x.addressLine1},"), Some(s"${x.addressLine2},"), x.addressLine3.map(y => s"$y,"),
      x.addressLine4.map(y => s"$y,"), x.postcode.map(y => s"$y,"), Some(country)).flatten
  }

  def uniqueTaxReference(index: Int): Seq[AnswerRow] = userAnswers.get(UniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) =>
      Seq(
        AnswerRow("uniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.Yes}"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("uniqueTaxReference.utr.checkYourAnswersLabel", Seq(utr), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url)
      )
    case Some(UniqueTaxReference.No(reason)) =>
      Seq(
        AnswerRow("uniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.No}"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("uniqueTaxReference.reason.checkYourAnswersLabel", Seq(reason), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def establisherNino(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherNinoId(index)) match {
    case Some(Yes(nino)) =>
      Seq(
        AnswerRow("establisherNino.checkYourAnswersLabel", Seq(s"${EstablisherNino.Yes}"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("establisherNino.nino.checkYourAnswersLabel", Seq(nino), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url)
      )
    case Some(No(reason)) =>
      Seq(
        AnswerRow("establisherNino.checkYourAnswersLabel", Seq(s"${EstablisherNino.No}"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("establisherNino.reason.checkYourAnswersLabel", Seq(reason), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def contactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(ContactDetailsId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(s"${x.emailAddress}"), false,
          controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("contactDetails.phoneNumber.checkYourAnswersLabel", Seq(s"${x.phoneNumber}"), false,
          controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def establisherDetails(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherDetailsId(index)) match {
    case Some(details) =>
      Seq(
        AnswerRow("establisherDetails.name.checkYourAnswersLabel", Seq(s"${details.firstName} ${details.lastName}"), false,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("establisherDetails.dateOfBirth.checkYourAnswersLabel", Seq(s"${DateHelper.formatDate(details.date)}"), false,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def addressYears(index: Int): Seq[AnswerRow] = {
    userAnswers.get[AddressYears](AddressYearsId(index)) match {
      case Some(x) =>
        Seq(
          AnswerRow("addressYears.checkYourAnswersLabel", Seq(s"messages__common__$x"), true,
            controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, Index(index)).url)
        )
      case _ => Nil
    }
  }

  def establisherKind(index:Int): Option[AnswerRow] = userAnswers.get(EstablisherKindId(index)) match {
    case Some(x) => Some(AnswerRow("establisherKind.checkYourAnswersLabel", Seq(s"${x.toString}"),false,
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def schemeEstablishedCountry: Option[AnswerRow] = userAnswers.get(SchemeEstablishedCountryId) map {
    x => AnswerRow("schemeEstablishedCountry.checkYourAnswersLabel", Seq(s"$x"), false,
      routes.SchemeEstablishedCountryController.onPageLoad(CheckMode).url)
  }

  def uKBankAccount: Option[AnswerRow] = userAnswers.get(UKBankAccountId) map {
    x => AnswerRow("uKBankAccount.checkYourAnswersLabel", if(x) Seq("site.yes") else Seq("site.no"), true,
      routes.UKBankAccountController.onPageLoad(CheckMode).url)
  }

  def uKBankDetails: Option[AnswerRow] = userAnswers.get(UKBankDetailsId) map {
    x => AnswerRow("uKBankDetails.checkYourAnswersLabel", Seq(s"${x.accountName} ${x.bankName}"), false,
      routes.UKBankDetailsController.onPageLoad(CheckMode).url)
  }

  def benefits: Option[AnswerRow] = userAnswers.get(BenefitsId) map {
    x => AnswerRow("benefits.checkYourAnswersLabel", Seq(s"benefits.$x"), true,
      routes.BenefitsController.onPageLoad(CheckMode).url)
  }

  def benefitsInsurer: Option[AnswerRow] = userAnswers.get(BenefitsInsurerId) map {
    x => AnswerRow("benefitsInsurer.checkYourAnswersLabel", Seq(s"${x.companyName} ${x.policyNumber}"), false,
      routes.BenefitsInsurerController.onPageLoad(CheckMode).url)
  }

  def membership: Option[AnswerRow] = userAnswers.get(MembershipId) map {
    x => AnswerRow("membership.checkYourAnswersLabel", Seq(s"membership.$x"), true,
      routes.MembershipController.onPageLoad(CheckMode).url)
  }

  def membershipFuture: Option[AnswerRow] = userAnswers.get(MembershipFutureId) map {
    x => AnswerRow("membershipFuture.checkYourAnswersLabel", Seq(s"membershipFuture.$x"), true,
      routes.MembershipFutureController.onPageLoad(CheckMode).url)
  }

  def investmentRegulated: Option[AnswerRow] = userAnswers.get(InvestmentRegulatedId) map {
    x => AnswerRow("investmentRegulated.checkYourAnswersLabel", Seq(if(x) "site.yes" else "site.no"), true,
      routes.InvestmentRegulatedController.onPageLoad(CheckMode).url)
  }

  def securedBenefits: Option[AnswerRow] = userAnswers.get(SecuredBenefitsId) map {
    x => AnswerRow("securedBenefits.checkYourAnswersLabel", Seq(if(x) "site.yes" else "site.no"), true,
      routes.SecuredBenefitsController.onPageLoad(CheckMode).url)
  }

  def occupationalPensionScheme: Option[AnswerRow] = userAnswers.get(OccupationalPensionSchemeId) map {
    x => AnswerRow("occupationalPensionScheme.checkYourAnswersLabel", Seq(if(x) "site.yes" else "site.no"), true,
      routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url)
  }

  def schemeDetails: Option[AnswerRow] = userAnswers.get(SchemeDetailsId) map {
    x => AnswerRow("schemeDetails.checkYourAnswersLabel", Seq(s"${x.schemeName} ${x.schemeType}"), false,
      routes.SchemeDetailsController.onPageLoad(CheckMode).url)
  }
}
