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

import identifiers.TypedIdentifier
import identifiers.register.SchemeEstablishedCountryId
import models.address.Address
import models.register._
import models.register.establishers.individual.UniqueTaxReference
import models.requests.DataRequest
import models.{AddressYears, CompanyDetails, CompanyRegistrationNumber, ContactDetails}
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import viewmodels.AnswerRow

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(retrieveStringAnswer(id, string)),
              answerIsMessageKey = false,
              changeUrl
            ))
        }.getOrElse(Seq.empty[AnswerRow])
    }

  private def retrieveStringAnswer[I](id: I, stringValue: String)(implicit countryOptions: CountryOptions): String = {
    id match {
      case SchemeEstablishedCountryId =>
        countryOptions.options.find(_.value == stringValue).map(_.label).getOrElse(stringValue)
      case _ => stringValue
    }
  }

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          flag =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(if (flag) "site.yes" else "site.no"),
              answerIsMessageKey = true,
              changeUrl
            ))
        }.getOrElse(Seq.empty)
    }

  private def schemeDetails[I <: TypedIdentifier[SchemeDetails]](
                                                                  nameLabel: String,
                                                                  typeLabel: String
                                                                )(implicit rds: Reads[SchemeDetails]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(id).map {
          schemeDetails =>

            Seq(AnswerRow(
              nameLabel,
              Seq(s"${schemeDetails.schemeName}"),
              false,
              changeUrl
            ),
              AnswerRow(
                typeLabel,
                Seq(s"messages__scheme_details__type_${schemeDetails.schemeType}"),
                true,
                changeUrl
              )
            )
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }

  implicit def defaultSchemeDetails[I <: TypedIdentifier[SchemeDetails]](implicit rds: Reads[SchemeDetails]): CheckYourAnswers[I] = {

    val nameLabel = "messages__scheme_details__name_label"
    val typeLabel = "messages__scheme_details__type_legend_short"

    schemeDetails(nameLabel, typeLabel)
  }

  implicit def membership[I <: TypedIdentifier[Membership]](implicit rds: Reads[Membership]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        membership =>
          Seq(
            AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(s"messages__membership__$membership"),
              true,
              changeUrl
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }

  private def bankDetails[I <: TypedIdentifier[UKBankDetails]](
                                                                bankNameLabel: String,
                                                                accountNameLabel: String,
                                                                sortCodeLabel: String,
                                                                accountNumberLabel: String,
                                                                dateLabel: String
                                                              )(implicit rds: Reads[UKBankDetails]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        bankDetails =>
          Seq(
            AnswerRow(
              bankNameLabel,
              Seq(s"${bankDetails.bankName}"),
              false,
              changeUrl
            ),
            AnswerRow(
              accountNameLabel,
              Seq(s"${bankDetails.accountName}"),
              false,
              changeUrl
            ),
            AnswerRow(
              sortCodeLabel,
              Seq(s"${bankDetails.sortCode.first}-${bankDetails.sortCode.second}-${bankDetails.sortCode.third}"),
              false,
              changeUrl
            ),
            AnswerRow(
              accountNumberLabel,
              Seq(s"${bankDetails.accountNumber}"),
              false,
              changeUrl
            ),
            AnswerRow(
              dateLabel,
              Seq(s"${DateHelper.formatDate(bankDetails.date)}"),
              false,
              changeUrl
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }

  implicit def defaultBankDetails[I <: TypedIdentifier[UKBankDetails]](implicit rds: Reads[UKBankDetails]): CheckYourAnswers[I] = {
    val bankNameLabel = "messages__uk_bank_account_details__bank_name"
    val accountNameLabel = "messages__uk_bank_account_details__account_name"
    val sortCodeLabel = "messages__uk_bank_account_details__sort_code"
    val accountNumberLabel = "messages__uk_bank_account_details__account_number"
    val dateLabel = "bankAccountDate.checkYourAnswersLabel"

    bankDetails(bankNameLabel, accountNameLabel, sortCodeLabel, accountNumberLabel, dateLabel)
  }

  implicit def schemeBenefits[I <: TypedIdentifier[Benefits]](implicit rds: Reads[Benefits]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        benefits =>
          Seq(
            AnswerRow(
              "messages__benefits__title",
              Seq(s"messages__benefits__$benefits"),
              true,
              changeUrl
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }

  private def benefitsInsurer[I <: TypedIdentifier[BenefitsInsurer]](
                                                                      nameLabel: String,
                                                                      policyLabel: String
                                                                    )(implicit rds: Reads[BenefitsInsurer]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        benefitsInsurer =>
          Seq(
            AnswerRow(
              nameLabel,
              Seq(s"${benefitsInsurer.companyName}"),
              false,
              changeUrl
            ),
            AnswerRow(
              policyLabel,
              Seq(s"${benefitsInsurer.policyNumber}"),
              false,
              changeUrl
            )
          )
      }.getOrElse(Seq.empty[AnswerRow])
    }

  implicit def defaultBenefitsInsurer[I <: TypedIdentifier[BenefitsInsurer]](implicit rds: Reads[BenefitsInsurer]): CheckYourAnswers[I] = {
    val nameLabel = "messages__benefits_insurance__name"
    val policyLabel = "messages__benefits_insurance__policy"

    benefitsInsurer(nameLabel, policyLabel)
  }

  def companyDetails[I <: TypedIdentifier[CompanyDetails]](
                                                            nameLabel: String,
                                                            vatLabel: String,
                                                            payeLabel: String
                                                          )(implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          companyDetails =>

            val nameRow = AnswerRow(
              nameLabel,
              Seq(s"${companyDetails.companyName}"),
              false,
              changeUrl
            )

            val withVat = companyDetails.vatNumber.fold(Seq(nameRow)) { vat =>
              Seq(nameRow, AnswerRow(
                vatLabel,
                Seq(s"$vat"),
                false,
                changeUrl
              ))
            }

            companyDetails.payeNumber.fold(withVat) { paye =>
              withVat :+ AnswerRow(
                payeLabel,
                Seq(s"$paye"),
                false,
                changeUrl
              )
            }

        }.getOrElse(Seq.empty[AnswerRow])
    }

  implicit def defaultCompanyDetails[I <: TypedIdentifier[CompanyDetails]](implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] = {

    val nameLabel = "messages__common__cya__name"
    val vatLabel = "messages__common__cya__vat"
    val payeLabel = "messages__common__cya__paye"

    companyDetails(nameLabel, vatLabel, payeLabel)
  }

  def companyRegistrationNumber[I <: TypedIdentifier[CompanyRegistrationNumber]](
                                                                                  label: String
                                                                                )(implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        userAnswers.get(id) match {
          case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(
            AnswerRow(
              label,
              Seq(s"${CompanyRegistrationNumber.Yes}"),
              true,
              changeUrl
            ),
            AnswerRow(
              "messages__common__crn",
              Seq(s"$crn"),
              true,
              changeUrl
            ))
          case Some(CompanyRegistrationNumber.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${CompanyRegistrationNumber.No}"),
              true,
              changeUrl),
            AnswerRow(
              "messages__company__cya__crn_no_reason",
              Seq(s"$reason"),
              true,
              changeUrl
            ))
          case _ => Seq.empty[AnswerRow]
        }
      }
    }

  implicit def defaultCompanyRegistrationNumber[I <: TypedIdentifier[CompanyRegistrationNumber]](implicit rds: Reads[CompanyRegistrationNumber]):
  CheckYourAnswers[I] =
    companyRegistrationNumber("messages__company__cya__crn_yes_no")

  def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]](
                                                                    label: String
                                                                  )(implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) => Seq(
            AnswerRow(
              label,
              Seq(s"${UniqueTaxReference.Yes}"),
              false,
              changeUrl
            ),
            AnswerRow(
              "messages__establisher_individual_utr_cya_label",
              Seq(utr),
              false,
              changeUrl
            )
          )
          case Some(UniqueTaxReference.No(reason)) => Seq(
            AnswerRow(
              label,
              Seq(s"${UniqueTaxReference.No}"),
              false, changeUrl
            ),
            AnswerRow(
              "messages__establisher_individual_utr_reason_cya_label",
              Seq(reason),
              false,
              changeUrl
            ))
          case _ => Seq.empty[AnswerRow]
        }
    }

  implicit def defaultUniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]](implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] =
    uniqueTaxReference("messages__establisher_individual_utr_question_cya_label")

  def address[I <: TypedIdentifier[Address]](
                                              label: String
                                            )(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        def addressAnswer(address: Address): Seq[String] = {
          val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
          Seq(
            Some(s"${address.addressLine1},"),
            Some(s"${address.addressLine2},"),
            address.addressLine3.map(line3 => s"$line3,"),
            address.addressLine4.map(line4 => s"$line4,"),
            address.postcode.map(postCode => s"$postCode,"),
            Some(country)
          ).flatten
        }

        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            addressAnswer(address),
            false, changeUrl
          ))
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }

  implicit def defaultAddress[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] =
    address("messages__common__cya__address")

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](
                                                                 label: String
                                                               )(implicit rds: Reads[AddressYears]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map(addressYears =>
        Seq(AnswerRow(
          label,
          Seq(s"messages__common__$addressYears"),
          true,
          changeUrl
        ))).getOrElse(Seq.empty[AnswerRow])
    }

  implicit def defaultAddressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] =
    addressYears("messages__establisher_address_years__title")

  implicit def contactDetails[I <: TypedIdentifier[ContactDetails]](implicit rds: Reads[ContactDetails]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map {
        contactDetails =>
          Seq(
            AnswerRow(
              "messages__common__email",
              Seq(s"${contactDetails.emailAddress}"),
              false,
              changeUrl
            ),
            AnswerRow(
              "messages__common__phone",
              Seq(s"${contactDetails.phoneNumber}"),
              false,
              changeUrl
            ))
      }.getOrElse(Seq.empty[AnswerRow])
    }

  trait Ops[A] {
    def row(changeUrl: String)(implicit request: DataRequest[AnyContent], reads: Reads[A]): Seq[AnswerRow]
  }

  object Ops {
    implicit def toOps[I <: TypedIdentifier.PathDependent](id: I)(implicit ev: CheckYourAnswers[I]): Ops[id.Data] =
      new Ops[id.Data] {
        override def row(changeUrl: String)(implicit request: DataRequest[AnyContent], reads: Reads[id.Data]): Seq[AnswerRow] =
          ev.row(id)(changeUrl, request.userAnswers)
      }
  }

}
