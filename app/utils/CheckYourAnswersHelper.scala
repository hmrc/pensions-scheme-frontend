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

package utils

import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director._
import identifiers.register.establishers.individual._
import identifiers.register.establishers.{EstablisherKindId, company}
import identifiers.register.trustees.individual.{TrusteeAddressYearsId, TrusteeNinoId}
import models.Nino.{No, Yes}
import models.address.Address
import models.{UniqueTaxReference, _}
import viewmodels.{AnswerRow, Message}

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) extends Enumerable.Implicits {

  def haveAnyTrustees: Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.HaveAnyTrusteesId) map {
    x =>
      AnswerRow("haveAnyTrustees.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
        Some(controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(CheckMode).url))
  }

  def declaration: Option[AnswerRow] = userAnswers.get(identifiers.register.DeclarationId) map {
    x => AnswerRow("messages__declaration__checkYourAnswersLabel", Seq(s"$x"), false, Some(controllers.register.routes.DeclarationController.onPageLoad.url))
  }

  def moreThanTenTrustees: Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.MoreThanTenTrusteesId) map {
    x =>
      AnswerRow("moreThanTenTrustees.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
        Some(controllers.register.trustees.routes.MoreThanTenTrusteesController.onPageLoad(CheckMode).url))
  }

  def trusteePreviousAddress(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.individual.TrusteePreviousAddressId(index)) map {
    x => AnswerRow("trusteePreviousAddress.checkYourAnswersLabel", addressAnswer(x), false, Some(controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(CheckMode, index).url))
  }

  def trusteeContactDetails(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.individual.TrusteeContactDetailsId(index)) map {
    x => AnswerRow("messages__trusteeContactDetails__checkYourAnswersLabel", Seq(s"${x.emailAddress} ${x.phoneNumber}"), false, Some(controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(CheckMode, index).url))
  }

  def trusteeNino(index: Int): Seq[AnswerRow] = userAnswers.get(TrusteeNinoId(index)) match {
    case Some(Yes(nino)) =>
      Seq(
        AnswerRow("messages__trusteeNino__nino_question_cya_label", Seq(s"${Nino.Yes}"), false,
          Some(controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(CheckMode, Index(index)).url)),
        AnswerRow("messages__trusteeNino__nino_cya_label", Seq(nino), false,
          Some(controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(CheckMode, Index(index)).url))
      )
    case Some(No(reason)) =>
      Seq(
        AnswerRow("messages__trusteeNino__nino_question_cya_label", Seq(s"${Nino.No}"), false,
          Some(controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(CheckMode, Index(index)).url)),
        AnswerRow("messages__trusteeNino__nino_reason_cya_label", Seq(reason), false,
          Some(controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(CheckMode, Index(index)).url))
      )
    case _ => Nil
  }

  def trusteeIndividualAddressYears(index: Int): Seq[AnswerRow] =
    userAnswers.get(TrusteeAddressYearsId(index)) match {
      case Some(x) => Seq(AnswerRow("messages__trusteeAddressYears__cya_label", Seq(s"messages__common__$x"), true,
        Some(controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(CheckMode, Index(index)).url)))
      case _ => Seq.empty
    }

  def individualPostCodeLookup(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.individual.IndividualPostCodeLookupId(index)) map {
    x => AnswerRow("individualPostCodeLookup.checkYourAnswersLabel", Seq(s"$x"), false, Some(controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(CheckMode, index).url))
  }

  def uniqueTaxReference(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.individual.UniqueTaxReferenceId(index)) map {
    x => AnswerRow("messages__uniqueTaxReference__checkYourAnswersLabel", Seq(s"uniqueTaxReference.$x"), true, Some(controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, index).url))
  }

  def trusteeKind(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.TrusteeKindId(index)) map {
    x => AnswerRow("messages__trusteeKind__checkYourAnswersLabel", Seq(s"trusteeKind.$x"), true, Some(controllers.register.trustees.routes.TrusteeKindController.onPageLoad(CheckMode, index).url))
  }

  def companyRegistrationNumberTrustee(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.trustees.company.CompanyRegistrationNumberId(index)) map {
    x => AnswerRow("messages__companyRegistrationNumber__checkYourAnswersLabel", Seq(s"companyRegistrationNumber.$x"), true, Some(controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, index).url))
  }

  def directorPreviousAddressPostcodeLookup(mode: Mode, establisherIndex: Int, directorIndex: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.establishers.company.director.DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex)) map {
    x => AnswerRow("directorPreviousAddressPostcodeLookup.checkYourAnswersLabel", Seq(s"$x"), false, Some(controllers.register.establishers.company.director.routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, establisherIndex, directorIndex, None).url))
  }

  def directorUniqueTaxReference(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(DirectorUniqueTaxReferenceId(establisherIndex, directorIndex)) match {
      case Some(UniqueTaxReference.Yes(utr)) => Seq(
        AnswerRow("messages__director__cya__utr_yes_no", Seq(s"${UniqueTaxReference.Yes}"), false,
          Some(controllers.register.establishers.company.director.routes.DirectorUniqueTaxReferenceController.onPageLoad(
            CheckMode, Index(establisherIndex), Index(directorIndex), None
          ).url),"messages__visuallyhidden__director__utr_yes_no"),
        AnswerRow("messages__director__cya__utr", Seq(s"$utr"), false,
          Some(controllers.register.establishers.company.director.routes.DirectorUniqueTaxReferenceController.onPageLoad(
            CheckMode, Index(establisherIndex), Index(directorIndex), None
          ).url),"messages__visuallyhidden__director__utr")
      )
      case Some(UniqueTaxReference.No(reason)) => Seq(
        AnswerRow("messages__director__cya__utr_yes_no", Seq(s"${UniqueTaxReference.No}"), false,
          Some(controllers.register.establishers.company.director.routes.DirectorUniqueTaxReferenceController.onPageLoad(
            CheckMode, Index(establisherIndex), Index(directorIndex), None
          ).url),"messages__visuallyhidden__director__utr_yes_no"),
        AnswerRow("messages__director__cya__utr_no_reason", Seq(s"$reason"), false,
          Some(controllers.register.establishers.company.director.routes.DirectorUniqueTaxReferenceController.onPageLoad(
            CheckMode, Index(establisherIndex), Index(directorIndex), None
          ).url),"messages__visuallyhidden__director__utr_no")
      )
      case _ => Seq.empty
    }

  def companyPreviousAddress(index: Int): Seq[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.CompanyPreviousAddressId(index)) match {
      case Some(x) => Seq(AnswerRow("messages__common__cya__previous_address", addressAnswer(x), false,
        Some(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, None, index).url),"messages__visuallyhidden__establisher__previous_address"))
      case _ => Nil
    }

  def addCompanyDirectors(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.establishers.company.AddCompanyDirectorsId(index)) map {
    x => AnswerRow("addCompanyDirectors.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true, Some(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode, None, index).url))
  }

  def companyPreviousAddressList(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.establishers.company.CompanyPreviousAddressListId(index)) map {
    x => AnswerRow("messages__companyPreviousAddressList__checkYourAnswersLabel", Seq(s"companyPreviousAddressList.$x"), true, Some(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, None, index).url))
  }

  def companyPreviousAddressPostcodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.CompanyPreviousAddressPostcodeLookupId(index)) map {
      x =>
        AnswerRow("companyPreviousAddressPostcodeLookup.checkYourAnswersLabel", Seq(s"$x"), false,
          Some(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, None, index).url))
    }

  def directorContactDetails(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(DirectorContactDetailsId(establisherIndex, directorIndex)).fold(Seq.empty[AnswerRow]) { x =>
      Seq(
        AnswerRow(
          "messages__common__email",
          Seq(x.emailAddress),
          false,
          Some(controllers.register.establishers.company.director.routes.DirectorContactDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex), None).url),
          "messages__visuallyhidden__director__email_address"
        ), AnswerRow(
          "messages__common__phone",
          Seq(x.phoneNumber),
          false,
          Some(controllers.register.establishers.company.director.routes.DirectorContactDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex), None).url),
          "messages__visuallyhidden__director__phone_number"
        )
      )
    }


  def directorNino(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] = userAnswers.get(DirectorNinoId(establisherIndex,
    directorIndex)) match {
    case Some(Nino.Yes(nino)) =>
      Seq(
        AnswerRow("messages__director_nino_question_cya_label", Seq(s"${Nino.Yes}"), false,
          Some(controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
          Index(directorIndex), None).url),
          "messages__visuallyhidden__director__nino_yes_no"),
        AnswerRow("messages__director_nino_cya_label", Seq(nino), false,
          Some(controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
          Index(directorIndex), None).url),
          "messages__visuallyhidden__director__nino")
      )
    case Some(Nino.No(reason)) =>
      Seq(
        AnswerRow("messages__director_nino_question_cya_label", Seq(s"${Nino.No}"), false,
          Some(controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
          Index(directorIndex), None).url),
          "messages__visuallyhidden__director__nino_yes_no"),
        AnswerRow("messages__director_nino_reason_cya_label", Seq(reason), false,
          Some(controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(CheckMode, Index(establisherIndex),
          Index(directorIndex), None).url),
          "messages__visuallyhidden__director__nino_no")
      )
    case _ => Nil
  }

  def directorDetails(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(DirectorDetailsId(establisherIndex, directorIndex)).fold(Seq.empty[AnswerRow]) { details =>
      val directorName: String = Seq(Some(details.firstName), details.middleName, Some(details.lastName)).flatten.mkString(" ")
      Seq(
        AnswerRow(
          "messages__common__cya__name",
          Seq(directorName),
          false,
          Some(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex), None).url),
          Message("messages__visuallyhidden__common__name", directorName)
        ),
        AnswerRow(
          "messages__common__dob",
          Seq(s"${DateHelper.formatDate(details.date)}"),
          false,
          Some(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex), None).url),
          Message("messages__visuallyhidden__common__dob", directorName)
        ))
    }

  def directorAddress(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(DirectorAddressId(establisherIndex, directorIndex)).fold(Seq.empty[AnswerRow]) { x =>
      Seq(AnswerRow(
        "messages__common__cya__address",
        addressAnswer(x),
        false,
        Some(controllers.register.establishers.company.director.routes.DirectorAddressController.onPageLoad(CheckMode, establisherIndex, directorIndex, None).url),
        "messages__visuallyhidden__director__address"
      ))
    }

  def directorPreviousAddress(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.director.DirectorPreviousAddressId(establisherIndex, directorIndex)) match {
      case Some(x) => Seq(AnswerRow("messages__common__cya__previous_address", addressAnswer(x), false,
        Some(controllers.register.establishers.company.director.routes.DirectorPreviousAddressController.onPageLoad(CheckMode, establisherIndex, directorIndex, None).url),
        "messages__visuallyhidden__director__previous_address"))
      case _ => Nil
    }

  def directorAddressYears(establisherIndex: Int, directorIndex: Int): Seq[AnswerRow] =
    userAnswers.get(identifiers.register.establishers.company.director.DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(x) => Seq(AnswerRow(
        "messages__director_address_years__cya",
        Seq(s"messages__common__$x"),
        true,
        Some(controllers.register.establishers.company.director.routes.DirectorAddressYearsController.onPageLoad(
        CheckMode, establisherIndex, directorIndex, None).url),
        "messages__visuallyhidden__director__address_years"
      ))
      case _ => Seq.empty
    }

  def companyAddress(index: Int): Seq[AnswerRow] = userAnswers.get(company.CompanyAddressId(index)).fold(Seq.empty[AnswerRow]) { x =>
    Seq(AnswerRow(
      "messages__companyAddress__checkYourAnswersLabel",
      addressAnswer(x),
      false,
      Some(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(CheckMode, None, Index(index)).url),
      "messages__visuallyhidden__establisher__address"
    ))
  }

  def companyAddressYears(index: Int): Seq[AnswerRow] =
    userAnswers.get(CompanyAddressYearsId(index)) match {
      case Some(x) => Seq(AnswerRow("companyAddressYears.checkYourAnswersLabel", Seq(s"messages__common__$x"), true,
        Some(controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__address_years"))
      case _ => Seq.empty
    }

  def companyUniqueTaxReference(index: Int): Seq[AnswerRow] =
    userAnswers.get(CompanyUniqueTaxReferenceId(index)) match {
      case Some(UniqueTaxReference.Yes(utr)) => Seq(AnswerRow("messages__company__cya__utr_yes_no", Seq(s"${UniqueTaxReference.Yes}"), false,
        Some(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__utr_yes_no"),
        AnswerRow("messages__company__cya__utr", Seq(s"$utr"), false,
          Some(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__utr"))

      case Some(UniqueTaxReference.No(reason)) => Seq(AnswerRow("messages__company__cya__utr_yes_no", Seq(s"${UniqueTaxReference.No}"), false,
        Some(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__utr_yes_no"),
        AnswerRow("messages__company__cya__utr_no_reason", Seq(s"$reason"), false,
          Some(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__utr_no"))

      case _ => Seq.empty
    }

  def companyContactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(CompanyContactDetailsId(index)) match {
    case Some(x) => Seq(AnswerRow("messages__common__email", Seq(s"${x.emailAddress}"), false,
      Some(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__email_address"),
      AnswerRow("messages__common__phone", Seq(s"${x.phoneNumber}"), false,
        Some(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(CheckMode, None, Index(index)).url),"messages__visuallyhidden__establisher__phone_number"))

    case _ => Seq.empty
  }

  def establisherNino(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherNinoId(index)) match {
    case Some(Yes(nino)) =>
      Seq(
        AnswerRow("messages__establisher_individual_nino_question_cya_label", Seq(s"${Nino.Yes}"), false,
          Some(controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
          "messages__visuallyhidden__establisher__nino_yes_no"),
        AnswerRow("messages__establisher_individual_nino_cya_label", Seq(nino), false,
          Some(controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
          "messages__visuallyhidden__establisher__nino")
      )
    case Some(No(reason)) =>
      Seq(
        AnswerRow("messages__establisher_individual_nino_question_cya_label", Seq(s"${Nino.No}"), false,
          Some(controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
          "messages__visuallyhidden__establisher__nino_yes_no"),
        AnswerRow("messages__establisher_individual_nino_reason_cya_label", Seq(reason), false,
          Some(controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, Index(index)).url),
          "messages__visuallyhidden__establisher__nino_no")
      )
    case _ => Nil
  }

  def contactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(ContactDetailsId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow(
          "messages__establisher_individual_email_cya_label", Seq(s"${x.emailAddress}"), false,
          Some(controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url),"messages__visuallyhidden__establisher__email_address"),
        AnswerRow("messages__establisher_individual_phone_cya_label", Seq(s"${x.phoneNumber}"), false,
          Some(controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, Index(index)).url),"messages__visuallyhidden__establisher__phone_number")
      )
    case _ => Nil
  }

  def establisherDetails(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherDetailsId(index)) match {
    case Some(details) =>
      val establisherName: String = Seq(Some(details.firstName), details.middleName, Some(details.lastName)).flatten.mkString(" ")
      Seq(
        AnswerRow("messages__establisher_individual_name_cya_label",
          Seq(establisherName),
          false,
          Some(controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url),
          Message("messages__visuallyhidden__common__name", establisherName)),
        AnswerRow("messages__establisher_individual_dob_cya_label",
          Seq(s"${DateHelper.formatDate(details.date)}"),
          false,
          Some(controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, Index(index)).url),
          Message("messages__visuallyhidden__common__dob", establisherName))
      )
    case _ => Nil
  }

  def addressYears(index: Int): Seq[AnswerRow] = {
    userAnswers.get[AddressYears](AddressYearsId(index)) match {
      case Some(x) =>
        Seq(
          AnswerRow("messages__establisher_individual_address_years_cya_label", Seq(s"messages__common__$x"), true,
            Some(controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, Index(index)).url),
            "messages__visuallyhidden__establisher__address_years")
        )
      case _ => Nil
    }
  }

  def address(index: Int): Seq[AnswerRow] = userAnswers.get(AddressId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("messages__establisher_individual_address_cya_label", addressAnswer(x), false,
          Some(controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, Index(index)).url),
          "messages__visuallyhidden__establisher__address")
      )
    case _ => Nil
  }

  def previousAddress(index: Int): Seq[AnswerRow] = userAnswers.get(PreviousAddressId(index)) match {
    case Some(x) =>
      Seq(
        AnswerRow("messages__establisher_individual_previous_address_cya_label", addressAnswer(x), false,
          Some(controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, Index(index)).url),
          "messages__visuallyhidden__establisher__previous_address")
      )
    case _ => Nil
  }

  def addressAnswer(address: Address): Seq[String] = {
    val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
    Seq(Some(address.addressLine1), Some(address.addressLine2), address.addressLine3,
      address.addressLine4, address.postcode, Some(country)).flatten
  }

  def establisherKind(index: Int): Seq[AnswerRow] = userAnswers.get(EstablisherKindId(index)) match {
    case Some(x) => Seq(AnswerRow("establisherKind.checkYourAnswersLabel", Seq(s"${x.toString}"), false,
      Some(controllers.register.establishers.routes.EstablisherKindController.onPageLoad(CheckMode, Index(index)).url)))
    case _ => Seq.empty
  }
}
