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

package utils.checkyouranswers

import identifiers.TypedIdentifier
import identifiers.register.SchemeEstablishedCountryId
import models._
import models.address.Address
import models.person.PersonDetails
import models.register._
import play.api.libs.json.Reads
import utils.{CountryOptions, DateHelper, UserAnswers}
import viewmodels.AnswerRow

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def nino[I <: TypedIdentifier[Nino]](implicit rds: Reads[Nino]): CheckYourAnswers[I] = NinoCYA()()

  implicit def companyRegistrationNumber[I <: TypedIdentifier[CompanyRegistrationNumber]](implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] = CompanyRegistrationNumberCYA()()

  implicit def schemeDetails[I <: TypedIdentifier[SchemeDetails]](implicit rds: Reads[SchemeDetails]): CheckYourAnswers[I] = SchemeDetailsCYA()()

  implicit def bankDetails[I <: TypedIdentifier[UKBankDetails]](implicit rds: Reads[UKBankDetails]): CheckYourAnswers[I] = BankDetailsCYA()()

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = AddressYearsCYA()()

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = AddressCYA()()

  implicit def benefitsInsurer[I <: TypedIdentifier[BenefitsInsurer]](implicit rds: Reads[BenefitsInsurer]): CheckYourAnswers[I] = BenefitsInsurerCYA()()

  implicit def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]](implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] = UniqueTaxReferenceCYA()()

  implicit def companyDetails[I <: TypedIdentifier[CompanyDetails]](implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] = CompanyDetailsCYA()()

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          flag =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(if (flag) "site.yes" else "site.no"),
              answerIsMessageKey = true,
              Some(changeUrl)
            ))
        }.getOrElse(Seq.empty)
    }
  }

  implicit def schemeBenefits[I <: TypedIdentifier[Benefits]](implicit rds: Reads[Benefits]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        benefits =>
          Seq(
            AnswerRow(
              "messages__benefits__title",
              Seq(s"messages__benefits__$benefits"),
              answerIsMessageKey = true,
              Some(changeUrl)
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

  implicit def contactDetails[I <: TypedIdentifier[ContactDetails]](implicit rds: Reads[ContactDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        contactDetails =>
          Seq(
            AnswerRow(
              "messages__common__email",
              Seq(s"${contactDetails.emailAddress}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__common__phone",
              Seq(s"${contactDetails.phoneNumber}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ))
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

  implicit def adviserDetails[I <: TypedIdentifier[AdviserDetails]](implicit rds: Reads[AdviserDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        adviserDetails =>
          Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${adviserDetails.adviserName}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__adviserDetails__email",
              Seq(s"${adviserDetails.emailAddress}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__adviserDetails__phone",
              Seq(s"${adviserDetails.phoneNumber}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ))
      }.getOrElse(Seq.empty[AnswerRow])
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
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__common__dob",
              Seq(DateHelper.formatDate(personDetails.date)),
              answerIsMessageKey = false,
              Some(changeUrl)
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
              Some(changeUrl)
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
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
            Some(changeUrl)
          )
        )
      } getOrElse Seq.empty[AnswerRow]
    }
  }

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(retrieveStringAnswer(id, string)),
              answerIsMessageKey = false,
              Some(changeUrl)
            ))
        }.getOrElse(Seq.empty[AnswerRow])
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
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__common__cya__vat",
              Seq(vat),
              answerIsMessageKey = false,
              Some(changeUrl)
            )
          )
          case Vat.No => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__vat",
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(changeUrl)
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
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__common__cya__paye",
              Seq(paye),
              answerIsMessageKey = false,
              Some(changeUrl)
            )
          )
          case Paye.No => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__paye",
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(changeUrl)
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  private def retrieveStringAnswer[I](id: I, stringValue: String)(implicit countryOptions: CountryOptions): String = {
    id match {
      case SchemeEstablishedCountryId =>
        countryOptions.options.find(_.value == stringValue).map(_.label).getOrElse(stringValue)
      case _ => stringValue
    }
  }

}

case class NinoCYA[I <: TypedIdentifier[Nino]](label: String = "messages__trusteeNino_question_cya_label") {

  def apply()(implicit rds: Reads[Nino]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id) match {
          case Some(Nino.Yes(nino)) => Seq(
            AnswerRow(
              label,
              Seq(s"${Nino.Yes}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__trusteeNino_nino_cya_label",
              Seq(nino),
              answerIsMessageKey = false,
              Some(changeUrl)
            )
          )
          case Some(Nino.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${Nino.No}"),
              answerIsMessageKey = false, Some(changeUrl)
            ),
            AnswerRow(
              "messages__trusteeNino_reason_cya_label",
              Seq(reason),
              answerIsMessageKey = false,
              Some(changeUrl)
            ))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }

}

case class CompanyRegistrationNumberCYA[I <: TypedIdentifier[CompanyRegistrationNumber]](label: String = "messages__company__cya__crn_yes_no") {

  def apply()(implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        userAnswers.get(id) match {
          case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(
            AnswerRow(
              label,
              Seq(s"${CompanyRegistrationNumber.Yes}"),
              answerIsMessageKey = true,
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__common__crn",
              Seq(s"$crn"),
              answerIsMessageKey = true,
              Some(changeUrl)
            ))
          case Some(CompanyRegistrationNumber.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${CompanyRegistrationNumber.No}"),
              answerIsMessageKey = true,
              Some(changeUrl)),
            AnswerRow(
              "messages__company__cya__crn_no_reason",
              Seq(s"$reason"),
              answerIsMessageKey = true,
              Some(changeUrl)
            ))
          case _ => Seq.empty[AnswerRow]
        }
      }
    }
  }

}

case class SchemeDetailsCYA[I <: TypedIdentifier[SchemeDetails]](
                                                                  nameLabel: String = "messages__scheme_details__name_label",
                                                                  typeLabel: String = "messages__scheme_details__type_legend_short"
                                                                ) {

  def apply()(implicit rds: Reads[SchemeDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(id).map {
          schemeDetails =>

            Seq(AnswerRow(
              nameLabel,
              Seq(s"${schemeDetails.schemeName}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
              AnswerRow(
                typeLabel,
                Seq(s"messages__scheme_details__type_${schemeDetails.schemeType}"),
                answerIsMessageKey = true,
                Some(changeUrl)
              )
            )
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

}

case class BankDetailsCYA[I <: TypedIdentifier[UKBankDetails]](
                                                                bankNameLabel: String = "messages__uk_bank_account_details__bank_name",
                                                                accountNameLabel: String = "messages__uk_bank_account_details__account_name",
                                                                sortCodeLabel: String = "messages__uk_bank_account_details__sort_code",
                                                                accountNumberLabel: String = "messages__uk_bank_account_details__account_number",
                                                                dateLabel: String = "bankAccountDate.checkYourAnswersLabel"
                                                              ) {

  def apply()(implicit rds: Reads[UKBankDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map {
        bankDetails =>
          Seq(
            AnswerRow(
              bankNameLabel,
              Seq(s"${bankDetails.bankName}"),
              answerIsMessageKey = false,
              changeUrl = Some(changeUrl)
            ),
            AnswerRow(
              accountNameLabel,
              Seq(s"${bankDetails.accountName}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              sortCodeLabel,
              Seq(s"${bankDetails.sortCode.first}-${bankDetails.sortCode.second}-${bankDetails.sortCode.third}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              accountNumberLabel,
              Seq(s"${bankDetails.accountNumber}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              dateLabel,
              Seq(s"${DateHelper.formatDate(bankDetails.date)}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

}

case class AddressYearsCYA[I <: TypedIdentifier[AddressYears]](label: String = "messages__establisher_address_years__title") {

  def apply()(implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map(addressYears =>
        Seq(AnswerRow(
          label,
          Seq(s"messages__common__$addressYears"),
          answerIsMessageKey = true,
          Some(changeUrl)
        ))).getOrElse(Seq.empty[AnswerRow])
    }
  }

}

case class AddressCYA[I <: TypedIdentifier[Address]](label: String = "messages__common__cya__address") {

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
            address.postcode.map(postCode => s"$postCode,"),
            Some(country)
          ).flatten
        }

        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            addressAnswer(address),
            answerIsMessageKey = false, Some(changeUrl)
          ))
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

}

case class BenefitsInsurerCYA[I <: TypedIdentifier[BenefitsInsurer]](
                                                                      nameLabel: String = "messages__benefits_insurance__name",
                                                                      policyLabel: String = "messages__benefits_insurance__policy"
                                                                    ) {

  def apply()(implicit rds: Reads[BenefitsInsurer]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        benefitsInsurer =>
          Seq(
            AnswerRow(
              nameLabel,
              Seq(s"${benefitsInsurer.companyName}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              policyLabel,
              Seq(s"${benefitsInsurer.policyNumber}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }
  }

}

case class UniqueTaxReferenceCYA[I <: TypedIdentifier[UniqueTaxReference]](label: String = "messages__establisher_individual_utr_question_cya_label") {

  def apply()(implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) => Seq(
            AnswerRow(
              label,
              Seq(s"${UniqueTaxReference.Yes}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            ),
            AnswerRow(
              "messages__establisher_individual_utr_cya_label",
              Seq(utr),
              answerIsMessageKey = false,
              Some(changeUrl)
            )
          )
          case Some(UniqueTaxReference.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${UniqueTaxReference.No}"),
              answerIsMessageKey = false, Some(changeUrl)
            ),
            AnswerRow(
              "messages__establisher_individual_utr_reason_cya_label",
              Seq(reason),
              answerIsMessageKey = false,
              Some(changeUrl)
            ))
          case _ => Seq.empty[AnswerRow]
        }
    }
  }

}

case class CompanyDetailsCYA[I <: TypedIdentifier[CompanyDetails]](
                                                                    nameLabel: String = "messages__common__cya__name",
                                                                    vatLabel: String = "messages__common__cya__vat",
                                                                    payeLabel: String = "messages__common__cya__paye"
                                                                  ) {

  def apply()(implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          companyDetails =>

            val nameRow = AnswerRow(
              nameLabel,
              Seq(s"${companyDetails.companyName}"),
              answerIsMessageKey = false,
              Some(changeUrl)
            )

            val withVat = companyDetails.vatNumber.fold(Seq(nameRow)) { vat =>
              Seq(nameRow, AnswerRow(
                vatLabel,
                Seq(s"$vat"),
                answerIsMessageKey = false,
                Some(changeUrl)
              ))
            }

            companyDetails.payeNumber.fold(withVat) { paye =>
              withVat :+ AnswerRow(
                payeLabel,
                Seq(s"$paye"),
                answerIsMessageKey = false,
                Some(changeUrl)
              )
            }

        }.getOrElse(Seq.empty[AnswerRow])
    }
  }

}
