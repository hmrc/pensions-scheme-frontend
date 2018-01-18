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

import models.{CheckMode, Index, UniqueTaxReference}
import viewmodels.AnswerRow
import controllers.register.routes

import scala.util.Success

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def companyDetails(index: Int): Option[AnswerRow] = userAnswers.companyDetails(index) match {
    case Success(Some(x)) => Some(AnswerRow("companyDetails.checkYourAnswersLabel", s"${x.companyName} ${x.vatNumber} ${x.payeNumber}", false,
      controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))
    case _ => None
  }

  def companyContactDetails(index: Int): Option[AnswerRow] = userAnswers.companyContactDetails(index) match {
    case Success(Some(x)) => Some(AnswerRow("companyContactDetails.checkYourAnswersLabel", s"${x.emailAddress} ${x.phoneNumber}", false,
      controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def uniqueTaxReference(index: Int): Option[AnswerRow] = userAnswers.uniqueTaxReference(index) match {
    case Success(Some(x)) => Some(AnswerRow("uniqueTaxReference.checkYourAnswersLabel", s"${UniqueTaxReference.Yes} ${UniqueTaxReference.No}", false,
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def contactDetails(index: Int): Option[AnswerRow] = userAnswers.contactDetails(index) match {
    case Success(Some(x)) => Some(AnswerRow("contactDetails.checkYourAnswersLabel", s"${x.emailAddress} ${x.phoneNumber}", false,
      controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def establisherDetails(index: Int): Option[AnswerRow] = userAnswers.establisherDetails(index) match {
    case Success(Some(x)) => Some(AnswerRow("establisherDetails.checkYourAnswersLabel", s"${x.firstName} ${x.lastName}", false,
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def addEstablisher: Option[AnswerRow] = userAnswers.addEstablisher map {
    x => AnswerRow("addEstablisher.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      controllers.register.establishers.routes.AddEstablisherController.onPageLoad(CheckMode).url)
  }

  def establisherKind(index:Int): Option[AnswerRow] = userAnswers.establisherKind(index) match {
    case Success(Some(x))=>Some(AnswerRow("establisherKind.checkYourAnswersLabel",s"${x.toString}",false,
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(CheckMode, Index(index)).url))
    case _ => None
  }

  def schemeEstablishedCountry: Option[AnswerRow] = userAnswers.schemeEstablishedCountry map {
    x => AnswerRow("schemeEstablishedCountry.checkYourAnswersLabel", s"$x", false,
      routes.SchemeEstablishedCountryController.onPageLoad(CheckMode).url)
  }

  def uKBankAccount: Option[AnswerRow] = userAnswers.uKBankAccount map {
    x => AnswerRow("uKBankAccount.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.UKBankAccountController.onPageLoad(CheckMode).url)
  }

  def uKBankDetails: Option[AnswerRow] = userAnswers.uKBankDetails map {
    x => AnswerRow("uKBankDetails.checkYourAnswersLabel", s"${x.accountName} ${x.bankName}", false,
      routes.UKBankDetailsController.onPageLoad(CheckMode).url)
  }

  def addressYears(index: Int): Option[AnswerRow] = {
    userAnswers.addressYears(index) match {
      case Success(Some(x)) => Some(AnswerRow("addressYears.checkYourAnswersLabel", s"addressYears.$x", true,
        controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, Index(index)).url))
      case _ => None
    }
  }

  def benefits: Option[AnswerRow] = userAnswers.benefits map {
    x => AnswerRow("benefits.checkYourAnswersLabel", s"benefits.$x", true,
      routes.BenefitsController.onPageLoad(CheckMode).url)
  }

  def benefitsInsurer: Option[AnswerRow] = userAnswers.benefitsInsurer map {
    x => AnswerRow("benefitsInsurer.checkYourAnswersLabel", s"${x.companyName} ${x.policyNumber}", false,
      routes.BenefitsInsurerController.onPageLoad(CheckMode).url)
  }

  def membership: Option[AnswerRow] = userAnswers.membership map {
    x => AnswerRow("membership.checkYourAnswersLabel", s"membership.$x", true,
      routes.MembershipController.onPageLoad(CheckMode).url)
  }

  def membershipFuture: Option[AnswerRow] = userAnswers.membershipFuture map {
    x => AnswerRow("membershipFuture.checkYourAnswersLabel", s"membershipFuture.$x", true,
      routes.MembershipFutureController.onPageLoad(CheckMode).url)
  }

  def investmentRegulated: Option[AnswerRow] = userAnswers.investmentRegulated map {
    x => AnswerRow("investmentRegulated.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.InvestmentRegulatedController.onPageLoad(CheckMode).url)
  }

  def securedBenefits: Option[AnswerRow] = userAnswers.securedBenefits map {
    x => AnswerRow("securedBenefits.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.SecuredBenefitsController.onPageLoad(CheckMode).url)
  }

  def occupationalPensionScheme: Option[AnswerRow] = userAnswers.occupationalPensionScheme map {
    x => AnswerRow("occupationalPensionScheme.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url)
  }

  def schemeDetails: Option[AnswerRow] = userAnswers.schemeDetails map {
    x => AnswerRow("schemeDetails.checkYourAnswersLabel", s"${x.schemeName} ${x.schemeType}", false,
      routes.SchemeDetailsController.onPageLoad(CheckMode).url)
  }
}
