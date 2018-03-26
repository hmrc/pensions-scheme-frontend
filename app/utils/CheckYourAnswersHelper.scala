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
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorContactDetailsId, DirectorDetailsId, DirectorNinoId, DirectorUniqueTaxReferenceId}
import identifiers.register.establishers.{EstablisherKindId, company}
import identifiers.register.establishers.individual._
import identifiers.register.establishers.company.director._
import identifiers.register.establishers.{EstablisherKindId, company}
import models.EstablisherNino.{No, Yes}
import models._
import models.address.Address
import models.register.CountryOptions
import models.register.establishers.company.director.DirectorNino
import models.register.establishers.individual.UniqueTaxReference
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) extends Enumerable.Implicits {

  def directorUniqueTaxReference(establisherIndex: Int, directorIndex:Int): Seq[AnswerRow] =
    userAnswers.get(DirectorUniqueTaxReferenceId(establisherIndex, directorIndex)) match {
      case Some(x) => Seq(AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.Yes} ${UniqueTaxReference.No}"), true,
        controllers.register.establishers.company.director.routes.DirectorUniqueTaxReferenceController.onPageLoad(
          CheckMode, establisherIndex, directorIndex).url))
      case _ => Seq.empty
    }

  def companyPreviousAddress(index: Int): Seq[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.CompanyPreviousAddressId(index)) match {
      case Some(x) => Seq(AnswerRow("messages__common__cya__previous_address", addressAnswer(x), false,
        controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index).url))
      case _ => Nil
    }

  def addCompanyDirectors(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.establishers.company.AddCompanyDirectorsId) map {
    x => AnswerRow("addCompanyDirectors.checkYourAnswersLabel", Seq(if(x) "site.yes" else "site.no"), true, controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode, index).url)
  }

  def companyPreviousAddressList(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.establishers.company.CompanyPreviousAddressListId(index)) map {
    x => AnswerRow("messages__companyPreviousAddressList__checkYourAnswersLabel", Seq(s"companyPreviousAddressList.$x"), true, controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, index).url)
  }

  def companyPreviousAddressPostcodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.CompanyPreviousAddressPostcodeLookupId(index)) map {
    x => AnswerRow("companyPreviousAddressPostcodeLookup.checkYourAnswersLabel", Seq(s"$x"), false,
      controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, index).url)
    }

  def directorContactDetails(establisherIndex:Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(DirectorContactDetailsId(establisherIndex, directorIndex)).fold(Seq.empty[AnswerRow]){ x =>
      Seq(AnswerRow(
        "directorContactDetails.checkYourAnswersLabel",
        Seq(s"${x.emailAddress} ${x.phoneNumber}"),
        false,
        controllers.register.establishers.company.director.routes.DirectorContactDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex)).url
      ))
  }


  def directorNino(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] = userAnswers.get(DirectorNinoId(establisherIndex,
    directorIndex)) match {
    case Some(models.register.establishers.company.director.DirectorNino.Yes(nino)) =>
      Seq(
        AnswerRow("messages__director_nino_question_cya_label", Seq(s"${DirectorNino.Yes}"), false,
          controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
            Index(directorIndex)).url),
        AnswerRow("messages__director_nino_cya_label", Seq(nino), false,
          controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
            Index(directorIndex)).url)
      )
    case Some(models.register.establishers.company.director.DirectorNino.No(reason)) =>
      Seq(
        AnswerRow("messages__director_nino_question_cya_label", Seq(s"${DirectorNino.No}"), false,
          controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
            Index(directorIndex)).url),
        AnswerRow("messages__director_nino_cya_label", Seq(reason), false,
          controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
            Index(directorIndex)).url)
      )
    case _ => Nil
  }

  def directorDetails(establisherIndex:Int,directorIndex:Int): Seq[AnswerRow] =
    userAnswers.get(DirectorDetailsId(establisherIndex,directorIndex)).fold(Seq.empty[AnswerRow]) { details =>
        Seq(
          AnswerRow(
            "messages__establisher_director_name_cya_label",
            Seq(Seq(Some(details.firstName), details.middleName, Some(details.lastName)).flatten.mkString(" ")),
            false,
            controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(CheckMode, Index(establisherIndex),Index(directorIndex)).url
          ),
          AnswerRow(
            "messages__establisher_director_dob_cya_label",
            Seq(s"${DateHelper.formatDate(details.date)}"),
            false,
            controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(CheckMode, Index(establisherIndex),Index(directorIndex)).url
          ))
    }

  def directorAddressYears(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.director.DirectorAddressYearsId(establisherIndex, directorIndex)).fold(Seq.empty[AnswerRow]){ x =>
      Seq(AnswerRow(
        "messages__companyDirectorAddressYears__checkYourAnswersLabel",
        Seq(s"messages__common__.$x"),
        true,
        controllers.register.establishers.company.director.routes.DirectorAddressYearsController.onPageLoad(
          CheckMode, establisherIndex, directorIndex).url
      ))
    }

  def companyAddress(index: Int): Seq[AnswerRow] = userAnswers.get(company.CompanyAddressId(index)).fold(Seq.empty[AnswerRow]){ x =>
    Seq(AnswerRow(
      "messages__companyAddress__checkYourAnswersLabel",
      addressAnswer(x),
      false,
      controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(CheckMode, Index(index)).url
    ))
  }

  def companyAddressYears(index: Int): Seq[AnswerRow] =
    userAnswers.get(CompanyAddressYearsId(index)) match {
      case Some(x) => Seq(AnswerRow("companyAddressYears.checkYourAnswersLabel", Seq(s"messages__common__.$x"), true,
        controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode, Index(index)).url))
      case _ => Seq.empty
    }

  def companyUniqueTaxReference(index: Int): Seq[AnswerRow] =
    userAnswers.get(CompanyUniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) => Seq(AnswerRow("messages__company__cya__utr_yes_no", Seq(s"${UniqueTaxReference.Yes}"), false,
      controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url),
      AnswerRow("messages__company__cya__utr", Seq(s"$utr"), false,
        controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url))

    case Some(UniqueTaxReference.No(reason)) => Seq(AnswerRow("messages__company__cya__utr_yes_no", Seq(s"${UniqueTaxReference.No}"), false,
      controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url),
      AnswerRow("messages__company__cya__utr_no_reason", Seq(s"$reason"), false,
        controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url))

    case _ => Seq.empty
  }

  def companyRegistrationNumber(index: Int): Seq[AnswerRow] =
    userAnswers.get(CompanyRegistrationNumberId(index)) match {
      case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(AnswerRow("messages__company__cya__crn_yes_no", Seq(s"${CompanyRegistrationNumber.Yes}"), true,
        controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__common__crn", Seq(s"$crn"), true,
          controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, Index(index)).url))
      case Some(CompanyRegistrationNumber.No(reason)) => Seq(AnswerRow("messages__company__cya__crn_yes_no", Seq(s"${CompanyRegistrationNumber.No}"), true,
        controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__company__cya__crn_no_reason", Seq(s"$reason"), true,
          controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, Index(index)).url))
      case _=> Seq.empty
  }

  def companyDetails(index: Int): Seq[AnswerRow] =
    userAnswers.get(CompanyDetailsId(index)) match {

      case Some(CompanyDetails(x, Some(y), Some(z))) => Seq(AnswerRow("messages__common__cya__name", Seq(s"$x"), false,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url),
        AnswerRow("messages__company__cya__vat", Seq(s"$y"), false,
          controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url),
        AnswerRow("messages__company__cya__paye_ern", Seq(s"$z"), false,
          controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))

      case Some(CompanyDetails(x, None, Some(z))) => Seq(AnswerRow("messages__common__cya__name", Seq(s"$x"), false,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url),
        AnswerRow("messages__company__cya__paye_ern", Seq(s"$z"), false,
          controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))

      case Some(CompanyDetails(x, Some(y), None)) => Seq(AnswerRow("messages__common__cya__name", Seq(s"$x"), false,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url),
        AnswerRow("messages__company__cya__vat", Seq(s"$y"), false,
          controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))

      case Some(CompanyDetails(x, None, None)) => Seq(AnswerRow("messages__common__cya__name", Seq(s"$x"), false,
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(CheckMode, Index(0)).url))
      case _ => Seq.empty
    }

  def companyContactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(CompanyContactDetailsId(index)) match {
    case Some(x) => Seq(AnswerRow("messages__common__email", Seq(s"${x.emailAddress}"), false,
      controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, Index(index)).url),
      AnswerRow("messages__common__phone", Seq(s"${x.phoneNumber}"), false,
        controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, Index(index)).url))

    case _ => Seq.empty
  }

  def uniqueTaxReference(index: Int): Seq[AnswerRow] = userAnswers.get(UniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) =>
      Seq(
        AnswerRow("messages__establisher_individual_utr_question_cya_label", Seq(s"${UniqueTaxReference.Yes}"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__establisher_individual_utr_cya_label", Seq(utr), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url)
      )
    case Some(UniqueTaxReference.No(reason)) =>
      Seq(
        AnswerRow("messages__establisher_individual_utr_question_cya_label", Seq(s"${UniqueTaxReference.No}"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__establisher_individual_utr_reason_cya_label", Seq(reason), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def establisherNino(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherNinoId(index)) match {
    case Some(Yes(nino)) =>
      Seq(
        AnswerRow("messages__establisher_individual_nino_question_cya_label", Seq(s"${EstablisherNino.Yes}"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__establisher_individual_nino_cya_label", Seq(nino), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url)
      )
    case Some(No(reason)) =>
      Seq(
        AnswerRow("messages__establisher_individual_nino_question_cya_label", Seq(s"${EstablisherNino.No}"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__establisher_individual_nino_reason_cya_label", Seq(reason), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def contactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(ContactDetailsId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("messages__establisher_individual_email_cya_label", Seq(s"${x.emailAddress}"), false,
          controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__establisher_individual_phone_cya_label", Seq(s"${x.phoneNumber}"), false,
          controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def establisherDetails(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherDetailsId(index)) match {
    case Some(details) =>
      Seq(
        AnswerRow("messages__establisher_individual_name_cya_label",
          Seq(Seq(Some(details.firstName), details.middleName, Some(details.lastName)).flatten.mkString(" ")),
          false,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url),
        AnswerRow("messages__establisher_individual_dob_cya_label",
          Seq(s"${DateHelper.formatDate(details.date)}"),
          false,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def addressYears(index: Int): Seq[AnswerRow] = {
    userAnswers.get[AddressYears](AddressYearsId(index)) match {
      case Some(x) =>
        Seq(
          AnswerRow("messages__establisher_individual_address_years_cya_label", Seq(s"messages__common__$x"), true,
            controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, Index(index)).url)
        )
      case _ => Nil
    }
  }

  def address(index: Int): Seq[AnswerRow] = userAnswers.get(AddressId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("messages__establisher_individual_address_cya_label", addressAnswer(x), false,
          controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def previousAddress(index: Int): Seq[AnswerRow] = userAnswers.get(PreviousAddressId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("messages__establisher_individual_previous_address_cya_label", addressAnswer(x), false,
          controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, Index(index)).url)
      )
    case _ => Nil
  }

  def addressAnswer(address: Address): Seq[String] = {
    val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
    Seq(Some(s"${address.addressLine1},"), Some(s"${address.addressLine2},"), address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"), address.postcode.map(postcode => s"$postcode,"), Some(country)).flatten
  }

  def establisherKind(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherKindId(index)) match {
    case Some(x) => Seq(AnswerRow("establisherKind.checkYourAnswersLabel", Seq(s"${x.toString}"), false,
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(CheckMode, Index(index)).url))
    case _ => Seq.empty
  }

  def schemeEstablishedCountry: Seq[AnswerRow] = userAnswers.get(SchemeEstablishedCountryId) match {
    case Some(x) => Seq(AnswerRow("schemeEstablishedCountry.checkYourAnswersLabel", Seq(s"$x"), false,
      routes.SchemeEstablishedCountryController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def uKBankAccount: Seq[AnswerRow] = userAnswers.get(UKBankAccountId) match {
    case Some(x) => Seq(AnswerRow("uKBankAccount.checkYourAnswersLabel", if (x) Seq("site.yes") else Seq("site.no"), true,
      routes.UKBankAccountController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def uKBankDetails: Seq[AnswerRow] = userAnswers.get(UKBankDetailsId) match {
    case Some(x) => Seq(AnswerRow("uKBankDetails.checkYourAnswersLabel", Seq(s"${x.accountName} ${x.bankName}"), false,
      routes.UKBankDetailsController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def benefits: Seq[AnswerRow] = userAnswers.get(BenefitsId) match {
    case Some(x) => Seq(AnswerRow("benefits.checkYourAnswersLabel", Seq(s"benefits.$x"), true,
      routes.BenefitsController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def benefitsInsurer: Seq[AnswerRow] = userAnswers.get(BenefitsInsurerId) match {
    case Some(x) => Seq(AnswerRow("benefitsInsurer.checkYourAnswersLabel", Seq(s"${x.companyName} ${x.policyNumber}"), false,
      routes.BenefitsInsurerController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def membership: Seq[AnswerRow] = userAnswers.get(MembershipId) match {
    case Some(x) => Seq(AnswerRow("membership.checkYourAnswersLabel", Seq(s"membership.$x"), true,
      routes.MembershipController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def membershipFuture: Seq[AnswerRow] = userAnswers.get(MembershipFutureId) match {
    case Some(x) => Seq(AnswerRow("membershipFuture.checkYourAnswersLabel", Seq(s"membershipFuture.$x"), true,
      routes.MembershipFutureController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def investmentRegulated: Seq[AnswerRow] = userAnswers.get(InvestmentRegulatedId) match {
    case Some(x) => Seq(AnswerRow("investmentRegulated.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
      routes.InvestmentRegulatedController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def securedBenefits: Seq[AnswerRow] = userAnswers.get(SecuredBenefitsId) match {
    case Some(x) => Seq(AnswerRow("securedBenefits.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
      routes.SecuredBenefitsController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def occupationalPensionScheme: Seq[AnswerRow] = userAnswers.get(OccupationalPensionSchemeId) match {
    case Some(x) => Seq(AnswerRow("occupationalPensionScheme.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
      routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }

  def schemeDetails: Seq[AnswerRow] = userAnswers.get(SchemeDetailsId) match {
    case Some(x) => Seq(AnswerRow("schemeDetails.checkYourAnswersLabel", Seq(s"${x.schemeName} ${x.schemeType}"), false,
      routes.SchemeDetailsController.onPageLoad(CheckMode).url))
    case _ => Seq.empty
  }
}
