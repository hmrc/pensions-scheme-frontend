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

package utils.checkyouranswers

import identifiers.{EstablishedCountryId, TypedIdentifier}
import models._
import models.address.Address
import models.person.PersonDetails
import models.register._
import play.api.libs.json.Reads
import utils.{CountryOptions, DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
  def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)
}

object CheckYourAnswers {

  implicit def nino[I <: TypedIdentifier[Nino]](implicit rds: Reads[Nino]): CheckYourAnswers[I] = NinoCYA()()

  implicit def companyRegistrationNumber[I <: TypedIdentifier[CompanyRegistrationNumber]]
  (implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] = CompanyRegistrationNumberCYA()()

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = AddressYearsCYA()()

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = AddressCYA()()

  implicit def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]]
  (implicit rds: Reads[UniqueTaxReference]):CheckYourAnswers[I] = UniqueTaxReferenceCYA()()

  implicit def dormant[I <: TypedIdentifier[DeclarationDormant]](implicit rds: Reads[DeclarationDormant]): CheckYourAnswers[I] = IsDormantCYA()()

  implicit def companyDetails[I <: TypedIdentifier[CompanyDetails]](implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] = CompanyDetailsCYA()()

  implicit def contactDetails[I <: TypedIdentifier[ContactDetails]](implicit rds: Reads[ContactDetails]): CheckYourAnswers[I] = ContactDetailsCYA()()

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] = StringCYA()()

  implicit def members[I <: TypedIdentifier[Members]](implicit rds: Reads[Members]): CheckYourAnswers[I] = MembersCYA()()

  case class StringCYA[I <: TypedIdentifier[String]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            string =>
              Seq(AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(retrieveStringAnswer(id, string)),
                answerIsMessageKey = false,
                Some(changeUrl),
                hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)
              ))
          }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

  case class BooleanCYA[I <: TypedIdentifier[Boolean]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[Boolean], countryOptions: CountryOptions): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            flag =>
              Seq(AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(if (flag) "site.yes" else "site.no"),
                answerIsMessageKey = true,
                Some(changeUrl),
                hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)
              ))
          }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          flag =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(if (flag) "site.yes" else "site.no"),
              answerIsMessageKey = true,
              Some(changeUrl),
              s"messages__visuallyhidden__${id.toString}"
            ))
        }.getOrElse(Seq.empty)
    }
  }


  case class SchemeTypeCYA[I <: TypedIdentifier[SchemeType]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[SchemeType]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            schemeType =>
              Seq(AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(s"messages__scheme_type_${schemeType.toString}"),
                answerIsMessageKey = true,
                Some(changeUrl),
                hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)
              ))
          }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

  implicit def typeOfBenefitsCYA[I <: TypedIdentifier[TypeOfBenefits]](implicit rds: Reads[TypeOfBenefits]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        typeOfBenefits =>
          Seq(
            AnswerRow(
              "messages__type_of_benefits_cya_label",
              Seq(s"messages__type_of_benefits__$typeOfBenefits"),
              answerIsMessageKey = true,
              Some(changeUrl),
              "messages__visuallyhidden__type_of_benefits_change"
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

  case class ContactDetailsCYA[I <: TypedIdentifier[ContactDetails]](changeEmailAddress: String = "messages__visuallyhidden__common__email_address",
                                                                     changePhoneNumber: String = "messages__visuallyhidden__common__phone_number") {

    def apply()(implicit rds: Reads[ContactDetails]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
          contactDetails =>
            Seq(
              AnswerRow(
                "messages__common__email",
                Seq(s"${contactDetails.emailAddress}"),
                answerIsMessageKey = false,
                Some(changeUrl),
                changeEmailAddress
              ),
              AnswerRow(
                "messages__common__phone",
                Seq(s"${contactDetails.phoneNumber}"),
                answerIsMessageKey = false,
                Some(changeUrl),
                changePhoneNumber
              ))
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

  implicit def personDetails[I <: TypedIdentifier[PersonDetails]](implicit rds: Reads[PersonDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        personDetails =>
          Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(personDetails.fullName),
              answerIsMessageKey = false,
              Some(changeUrl),
              Message("messages__visuallyhidden__common__name", personDetails.fullName)
            ),
            AnswerRow(
              "messages__common__dob",
              Seq(DateHelper.formatDate(personDetails.date)),
              answerIsMessageKey = false,
              Some(changeUrl),
              Message("messages__visuallyhidden__common__dob", personDetails.fullName)
            ))
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

  implicit def membership[I <: TypedIdentifier[Membership]](implicit rds: Reads[Membership]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        membership =>
          Seq(
            AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(s"messages__membership__$membership"),
              answerIsMessageKey = true,
              Some(changeUrl),
              s"messages__visuallyhidden__${id.toString}"
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

  case class MembersCYA[I <: TypedIdentifier[Members]](label: Option[String] = None,
                                                       hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[Members]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
          members =>
            Seq(
              AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(s"messages__members__$members"),
                answerIsMessageKey = true,
                Some(changeUrl),
                hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)
              )
            )
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

  implicit def partnershipDetails[I <: TypedIdentifier[PartnershipDetails]](implicit rds: Reads[PartnershipDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map { partnershipDetails =>
        Seq(
          AnswerRow(
            "messages__common__cya__name",
            Seq(partnershipDetails.name),
            answerIsMessageKey = false,
            Some(changeUrl),
            Message("messages__visuallyhidden__common__name", partnershipDetails.name)
          )
        )
      } getOrElse Seq.empty[AnswerRow]
    }
  }

  implicit def vat[I <: TypedIdentifier[Vat]](implicit r: Reads[Vat]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          case Vat.Yes(vat) => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__vat",
              Seq("site.yes"),
              answerIsMessageKey = true,
              Some(changeUrl),
              "messages__visuallyhidden__partnership__vat_yes_no"
            ),
            AnswerRow(
              "messages__common__cya__vat",
              Seq(vat),
              answerIsMessageKey = false,
              Some(changeUrl),
              "messages__visuallyhidden__partnership__vat_number"
            )
          )
          case Vat.No => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__vat",
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(changeUrl),
              "messages__visuallyhidden__partnership__vat_yes_no"
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  implicit def paye[I <: TypedIdentifier[Paye]](implicit r: Reads[Paye]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          case Paye.Yes(paye) => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__paye",
              Seq("site.yes"),
              answerIsMessageKey = true,
              Some(changeUrl),
              "messages__visuallyhidden__partnership__paye_yes_no"
            ),
            AnswerRow(
              "messages__common__cya__paye",
              Seq(paye),
              answerIsMessageKey = false,
              Some(changeUrl),
              "messages__visuallyhidden__partnership__paye_number"
            )
          )
          case Paye.No => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__paye",
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(changeUrl),
              "messages__visuallyhidden__partnership__paye_yes_no"
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  private def retrieveStringAnswer[I](id: I, stringValue: String)(implicit countryOptions: CountryOptions): String = {
    id match {
      case EstablishedCountryId =>
        countryOptions.options.find(_.value == stringValue).map(_.label).getOrElse(stringValue)
      case _ => stringValue
    }
  }

}

case class NinoCYA[I <: TypedIdentifier[Nino]](
                                                label: String = "messages__trusteeNino_question_cya_label",
                                                changeHasNino: String = "messages__visuallyhidden__trustee__nino_yes_no",
                                                changeNino: String = "messages__visuallyhidden__trustee__nino",
                                                changeNoNino: String = "messages__visuallyhidden__trustee__nino_no"
                                              ) {

  def apply()(implicit rds: Reads[Nino]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id) match {
          case Some(Nino.Yes(nino)) => Seq(
            AnswerRow(
              label,
              Seq(s"${Nino.Yes}"),
              answerIsMessageKey = false,
              Some(changeUrl),
              changeHasNino
            ),
            AnswerRow(
              "messages__trusteeNino_nino_cya_label",
              Seq(nino),
              answerIsMessageKey = false,
              Some(changeUrl),
              changeNino
            )
          )
          case Some(Nino.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${Nino.No}"),
              answerIsMessageKey = false, Some(changeUrl),
              changeHasNino
            ),
            AnswerRow(
              "messages__trusteeNino_reason_cya_label",
              Seq(reason),
              answerIsMessageKey = false,
              Some(changeUrl),
              changeNoNino
            ))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }

}

case class CompanyRegistrationNumberCYA[I <: TypedIdentifier[CompanyRegistrationNumber]](
                                                                                          label: String = "messages__company__cya__crn_yes_no",
                                                                                          changeHasCrn: String = "messages__visuallyhidden__establisher__crn_yes_no",
                                                                                          changeCrn: String = "messages__visuallyhidden__establisher__crn",
                                                                                          changeNoCrn: String = "messages__visuallyhidden__establisher__crn_no"
                                                                                        ) {

  def apply()(implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        userAnswers.get(id) match {
          case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(
            AnswerRow(
              label,
              Seq(s"${CompanyRegistrationNumber.Yes}"),
              answerIsMessageKey = true,
              Some(changeUrl),
              changeHasCrn
            ),
            AnswerRow(
              "messages__common__crn",
              Seq(s"$crn"),
              answerIsMessageKey = true,
              Some(changeUrl),
              changeCrn
            ))
          case Some(CompanyRegistrationNumber.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${CompanyRegistrationNumber.No}"),
              answerIsMessageKey = true,
              Some(changeUrl),
              changeHasCrn
            ),
            AnswerRow(
              "messages__company__cya__crn_no_reason",
              Seq(s"$reason"),
              answerIsMessageKey = true,
              Some(changeUrl),
              changeNoCrn
            ))
          case _ => Seq.empty[AnswerRow]
        }
      }
    }
  }

}

case class BankDetailsHnSCYA[I <: TypedIdentifier[BankAccountDetails]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

  def apply()(implicit rds: Reads[BankAccountDetails], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          bankDetails =>
            Seq(AnswerRow(
              label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
              Seq(bankDetails.bankName,
                bankDetails.accountName,
                s"${bankDetails.sortCode.first}-${bankDetails.sortCode.second}-${bankDetails.sortCode.third}",
                bankDetails.accountNumber),
              answerIsMessageKey = false,
              Some(changeUrl),
              hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)
            ))
        }.getOrElse(Seq.empty[AnswerRow])
    }
  }
}

case class AddressYearsCYA[I <: TypedIdentifier[AddressYears]](label: String = "messages__establisher_address_years__title",
                                                               changeAddressYears: String = "messages__visuallyhidden__common__address_years") {

  def apply()(implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map(addressYears =>
        Seq(AnswerRow(
          label,
          Seq(s"messages__common__$addressYears"),
          answerIsMessageKey = true,
          Some(changeUrl),
          changeAddressYears
        ))).getOrElse(Seq.empty[AnswerRow])
    }
  }

}

case class AddressCYA[I <: TypedIdentifier[Address]](
                                                      label: String = "messages__common__cya__address",
                                                      changeAddress: String = "messages__visuallyhidden__common__address"
                                                    ) {

  def apply()(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        def addressAnswer(address: Address): Seq[String] = {
          val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
          Seq(
            Some(address.addressLine1),
            Some(address.addressLine2),
            address.addressLine3,
            address.addressLine4,
            address.postcode,
            Some(country)
          ).flatten
        }

        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            addressAnswer(address),
            answerIsMessageKey = false, Some(changeUrl),
            changeAddress
          ))
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

}

case class UniqueTaxReferenceCYA[I <: TypedIdentifier[UniqueTaxReference]](
                                                                            label: String = "messages__establisher_individual_utr_question_cya_label",
                                                                            utrLabel: String = "messages__establisher_individual_utr_cya_label",
                                                                            reasonLabel: String = "messages__establisher_individual_utr_reason_cya_label",
                                                                            changeHasUtr: String = "messages__visuallyhidden__establisher__utr_yes_no",
                                                                            changeUtr: String = "messages__visuallyhidden__establisher__utr",
                                                                            changeNoUtr: String = "messages__visuallyhidden__establisher__utr_no"
                                                                          ) {

  def apply()(implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) => Seq(
            AnswerRow(
              label,
              Seq(s"${UniqueTaxReference.Yes}"),
              answerIsMessageKey = false,
              Some(changeUrl),
              changeHasUtr
            ),
            AnswerRow(
              utrLabel,
              Seq(utr),
              answerIsMessageKey = false,
              Some(changeUrl),
              changeUtr
            )
          )
          case Some(UniqueTaxReference.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${UniqueTaxReference.No}"),
              answerIsMessageKey = false, Some(changeUrl),
              changeHasUtr
            ),
            AnswerRow(
              reasonLabel,
              Seq(reason),
              answerIsMessageKey = false,
              Some(changeUrl),
              changeNoUtr
            ))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}

case class IsDormantCYA[I <: TypedIdentifier[DeclarationDormant]](
                                                                            label: String = "messages__company__cya__dormant",
                                                                            changeIsDormant: String = "messages__visuallyhidden__establisher__dormant"
                                                                          ) {

  def apply()(implicit rds: Reads[DeclarationDormant]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id) match {
          case Some(DeclarationDormant.Yes) => Seq(
            AnswerRow(
              label,
              Seq("site.yes"),
              answerIsMessageKey = true,
              Some(changeUrl),
              changeIsDormant
            )
          )
          case Some(DeclarationDormant.No) => Seq(
            AnswerRow(
              label,
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(changeUrl),
              changeIsDormant
            ))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }

}

case class CompanyDetailsCYA[I <: TypedIdentifier[CompanyDetails]](
                                                                    nameLabel: String = "messages__common__cya__name",
                                                                    vatLabel: String = "messages__common__cya__vat",
                                                                    changeVat: String = "messages__visuallyhidden__establisher__vat_number",
                                                                    payeLabel: String = "messages__common__cya__paye",
                                                                    changePaye: String = "messages__visuallyhidden__establisher__paye_number") {

  def apply()(implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          companyDetails =>

            val nameRow = AnswerRow(
              nameLabel,
              Seq(s"${companyDetails.companyName}"),
              answerIsMessageKey = false,
              Some(changeUrl),
              Message("messages__visuallyhidden__common__name", companyDetails.companyName)
            )

            val withVat = companyDetails.vatNumber.fold(Seq(nameRow)) { vat =>
              Seq(nameRow, AnswerRow(
                vatLabel,
                Seq(s"$vat"),
                answerIsMessageKey = false,
                Some(changeUrl),
                changeVat
              ))
            }

            companyDetails.payeNumber.fold(withVat) { paye =>
              withVat :+ AnswerRow(
                payeLabel,
                Seq(s"$paye"),
                answerIsMessageKey = false,
                Some(changeUrl),
                changePaye
              )
            }

        }.getOrElse(Seq.empty[AnswerRow])
    }
  }
}
