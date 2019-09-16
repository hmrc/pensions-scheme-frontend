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
import models.AddressYears.UnderAYear
import models._
import models.address.Address
import models.person.PersonDetails
import models.register._
import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.{CountryOptions, DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]

  def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def nino[I <: TypedIdentifier[Nino]](implicit rds: Reads[Nino]): CheckYourAnswers[I] = NinoCYA()()

  implicit def companyRegistrationNumber[I <: TypedIdentifier[CompanyRegistrationNumber]]
  (implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] = CompanyRegistrationNumberCYA()()

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = AddressYearsCYA()()

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = AddressCYA()()

  implicit def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]]
  (implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] = UniqueTaxReferenceCYA()()

  implicit def dormant[I <: TypedIdentifier[DeclarationDormant]](implicit rds: Reads[DeclarationDormant]): CheckYourAnswers[I] = IsDormantCYA()()

  implicit def companyDetails[I <: TypedIdentifier[CompanyDetails]]
  (implicit rds: Reads[CompanyDetails], messages: Messages): CheckYourAnswers[I] = CompanyDetailsCYA()()

  implicit def reference[I <: TypedIdentifier[ReferenceValue]]
  (implicit rds: Reads[ReferenceValue], messages: Messages): CheckYourAnswers[I] = ReferenceValueCYA()()

  implicit def contactDetails[I <: TypedIdentifier[ContactDetails]](implicit rds: Reads[ContactDetails]): CheckYourAnswers[I] = ContactDetailsCYA()()

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] = StringCYA()()

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] = BooleanCYA()()

  implicit def members[I <: TypedIdentifier[Members]](implicit rds: Reads[Members]): CheckYourAnswers[I] = MembersCYA()()

  implicit def paye[I <: TypedIdentifier[Paye]](implicit r: Reads[Paye]): CheckYourAnswers[I] = PayeCYA()()

  implicit def vat[I <: TypedIdentifier[Vat]](implicit r: Reads[Vat]): CheckYourAnswers[I] = VatCYA()()

  implicit def personDetails[I <: TypedIdentifier[PersonDetails]](
                             implicit rds: Reads[PersonDetails],
                             messages: Messages
                            ): CheckYourAnswers[I] = PersonalDetailsCYA()()

  case class StringCYA[I <: TypedIdentifier[String]](label: Option[String] = None, hiddenLabel: Option[String] = None,
                                                     displayAddLink: Boolean = false) {

    def apply()(implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        private def stringCYARow(id: I, changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
          userAnswers.get(id).map {
            string =>
              Seq(AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(retrieveStringAnswer(id, string)),
                answerIsMessageKey = false,
                changeUrl
              ))
          }.getOrElse(Seq.empty[AnswerRow])
        }

        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          stringCYARow(id, Some(Link("site.change", changeUrl,
            Some(hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)))), userAnswers)

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = stringCYARow(id, None, userAnswers)
      }
    }
  }

  case class BooleanCYA[I <: TypedIdentifier[Boolean]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[Boolean]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        private def booleanCYARow(id: I, changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
          userAnswers.get(id).map {
            flag =>
              Seq(AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(if (flag) "site.yes" else "site.no"),
                answerIsMessageKey = true,
                changeUrl
              ))
          }.getOrElse(Seq.empty[AnswerRow])
        }

        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = booleanCYARow(id,
          Some(Link("site.change", changeUrl,
            Some(hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)))),
          userAnswers)

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = booleanCYARow(id, None, userAnswers)
      }
    }
  }

  case class SchemeTypeCYA[I <: TypedIdentifier[SchemeType]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[SchemeType]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        private def schemeTypeCYARow(id: I, changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
          userAnswers.get(id).map {
            schemeType =>
              Seq(AnswerRow(
                label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq(s"messages__scheme_type_${schemeType.toString}"),
                answerIsMessageKey = true,
                changeUrl
              ))
          }.getOrElse(Seq.empty[AnswerRow])
        }

        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          schemeTypeCYARow(id, Some(Link("site.change", changeUrl,
            Some(hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)))), userAnswers)

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = schemeTypeCYARow(id, None, userAnswers)
      }
    }
  }


  case class ContactDetailsCYA[I <: TypedIdentifier[ContactDetails]](changeEmailAddress: String = "messages__visuallyhidden__common__email_address",
                                                                     changePhoneNumber: String = "messages__visuallyhidden__common__phone_number") {

    def apply()(implicit rds: Reads[ContactDetails]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map {
          contactDetails =>
            Seq(
              AnswerRow(
                "messages__common__email",
                Seq(s"${contactDetails.emailAddress}"),
                answerIsMessageKey = false,
                Some(Link("site.change", changeUrl,
                  Some(changeEmailAddress)))
              ),
              AnswerRow(
                "messages__common__phone",
                Seq(s"${contactDetails.phoneNumber}"),
                answerIsMessageKey = false,
                Some(Link("site.change", changeUrl,
                  Some(changePhoneNumber)))
              ))
        }.getOrElse(Seq.empty[AnswerRow])

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)
      }
    }
  }

  case class PersonalDetailsCYA[I <: TypedIdentifier[PersonDetails]]() {

    def apply()(implicit rds: Reads[PersonDetails], messages: Messages): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {

        private def personDetailsCYARow(personDetails: PersonDetails, changeUrlName: Option[Link], changeUrlDob: Option[Link]): Seq[AnswerRow] = {
          Seq(
            AnswerRow("messages__common__cya__name", Seq(personDetails.fullName), answerIsMessageKey = false, changeUrlName),
            AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(personDetails.date)), answerIsMessageKey = false, changeUrlDob)
          )
        }

        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map { personDetails =>
          personDetailsCYARow(personDetails,
            Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__name", personDetails.fullName).resolve))),
            Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__dob", personDetails.fullName).resolve)))
          )
        }.getOrElse(Seq.empty[AnswerRow])

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map { personDetails =>
          personDetailsCYARow(personDetails, None, None)
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }
  }

  case class PartnershipDetailsCYA[I <: TypedIdentifier[PartnershipDetails]]() {

    def apply()(implicit rds: Reads[PersonDetails], messages: Messages): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map { partnershipDetails =>
          Seq(AnswerRow("messages__common__cya__name", Seq(partnershipDetails.name), answerIsMessageKey = false,
            Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__name", partnershipDetails.name).resolve)))))
        } getOrElse Seq.empty[AnswerRow]

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map { partnershipDetails =>
          Seq(AnswerRow("messages__common__cya__name", Seq(partnershipDetails.name), answerIsMessageKey = false, None))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

  case class MembersCYA[I <: TypedIdentifier[Members]](label: Option[String] = None,
                                                       hiddenLabel: Option[String] = None) {

    def apply()(implicit rds: Reads[Members]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {

        private def memberCYARow(id: I, userAnswers: UserAnswers, changeUrl: Option[Link]): Seq[AnswerRow] = {
          userAnswers.get(id).map { members =>
            Seq(AnswerRow(
              label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
              Seq(s"messages__members__$members"),
              answerIsMessageKey = true,
              changeUrl
            ))
          }.getOrElse(Seq.empty[AnswerRow])
        }

        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = memberCYARow(id, userAnswers,
          Some(Link("site.change", changeUrl,
            Some(hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel)))))

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = memberCYARow(id, userAnswers, None)
      }
    }
  }

  implicit def partnershipDetails[I <: TypedIdentifier[PartnershipDetails]](implicit rds: Reads[PartnershipDetails],
                                                                            messages: Messages): CheckYourAnswers[I] = PartnershipDetailsCYA()()

  case class VatCYA[I <: TypedIdentifier[Vat]](labelYesNo: Option[String] = Some("messages__partnership__checkYourAnswers__vat"),
                                               hiddenLabelYesNo: String = "messages__visuallyhidden__partnership__vat_yes_no",
                                               hiddenLabelVat: String = "messages__visuallyhidden__partnership__vat_number") {
    def apply()(implicit rds: Reads[Vat]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {

        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            case Vat.Yes(vat) => Seq(
              AnswerRow(labelYesNo.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq("site.yes"), answerIsMessageKey = true, Some(Link("site.change", changeUrl, Some(hiddenLabelYesNo)))),
              AnswerRow("messages__common__cya__vat", Seq(vat), answerIsMessageKey = false,
                Some(Link("site.change", changeUrl, Some(hiddenLabelVat))))
            )
            case Vat.No => Seq(
              AnswerRow(labelYesNo.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq("site.no"), answerIsMessageKey = true, Some(Link("site.change", changeUrl, Some(hiddenLabelYesNo))))
            )
          } getOrElse Seq.empty[AnswerRow]

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            case Vat.Yes(vat) => Seq(AnswerRow("messages__common__cya__vat", Seq(vat), answerIsMessageKey = false, None))
            case Vat.No => Seq(AnswerRow("messages__common__cya__vat", Seq("site.not_entered"), answerIsMessageKey = true,
              Some(Link("site.add", changeUrl, Some(hiddenLabelVat)))))
          } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

  case class PayeCYA[I <: TypedIdentifier[Paye]](labelYesNo: Option[String] = Some("messages__partnership__checkYourAnswers__paye"),
                                                 hiddenLabelYesNo: String = "messages__visuallyhidden__partnership__paye_yes_no",
                                                 hiddenLabelPaye: String = "messages__visuallyhidden__partnership__paye_number") {
    def apply()(implicit rds: Reads[Paye]): CheckYourAnswers[I] = {
      new CheckYourAnswers[I] {
        override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            case Paye.Yes(paye) => Seq(
              AnswerRow(labelYesNo.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq("site.yes"), answerIsMessageKey = true, Some(Link("site.change", changeUrl, Some(hiddenLabelYesNo)))),
              AnswerRow("messages__common__cya__paye", Seq(paye), answerIsMessageKey = false,
                Some(Link("site.change", changeUrl, Some(hiddenLabelPaye))))
            )
            case Paye.No => Seq(
              AnswerRow(labelYesNo.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
                Seq("site.no"), answerIsMessageKey = true, Some(Link("site.change", changeUrl, Some(hiddenLabelYesNo)))))
          } getOrElse Seq.empty[AnswerRow]

        override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
          userAnswers.get(id).map {
            case Paye.Yes(paye) => Seq(AnswerRow("messages__common__cya__paye", Seq(paye), answerIsMessageKey = false, None))
            case Paye.No => Seq(AnswerRow("messages__common__cya__paye", Seq("site.not_entered"), answerIsMessageKey = true,
              Some(Link("site.add", changeUrl, Some(hiddenLabelPaye)))))
          } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

  private def retrieveStringAnswer[I](id: I, stringValue: String)(implicit countryOptions: CountryOptions): String = {
    id match {
      case EstablishedCountryId =>
        countryOptions.options.find(_.value == stringValue).map(_.label).getOrElse(stringValue)
      case _ => stringValue
    }
  }

  def addLink(label: String, changeUrl: String, hiddenLabel: Option[String]): Seq[AnswerRow] = Seq(AnswerRow(label,
    Seq("site.not_entered"),
    answerIsMessageKey = true,
    Some(Link("site.add", changeUrl, hiddenLabel))))
}

case class NinoCYA[I <: TypedIdentifier[Nino]](
                                                label: String = "messages__trusteeNino_question_cya_label",
                                                reasonLabel: String = "messages__common__reason",
                                                changeHasNino: String = "messages__visuallyhidden__trustee__nino_yes_no",
                                                changeNino: String = "messages__visuallyhidden__trustee__nino",
                                                changeNoNino: String = "messages__visuallyhidden__trustee__nino_no"
                                              ) {

  def apply()(implicit rds: Reads[Nino]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {

      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(Nino.Yes(nino)) => Seq(
            AnswerRow(label, Seq(s"${Nino.Yes}"), answerIsMessageKey = false, Some(Link("site.change", changeUrl, Some(changeHasNino)))),
            AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeNino))))
          )
          case Some(Nino.No(reason)) => Seq(
            AnswerRow(label, Seq(s"${Nino.No}"), answerIsMessageKey = false, Some(Link("site.change", changeUrl, Some(changeHasNino)))),
            AnswerRow(reasonLabel, Seq(reason), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeNoNino)))))
          case _ => Seq.empty[AnswerRow]
        }
      }

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id) match {
        case Some(Nino.Yes(nino)) => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false, None))
        case Some(Nino.No(_)) => Seq(AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link("site.add", changeUrl, Some(changeNino)))))
        case _ => Seq.empty[AnswerRow]
      }
    }
  }
}

case class CompanyRegistrationNumberCYA[I <: TypedIdentifier[CompanyRegistrationNumber]](
                                        label: String = "messages__company__cya__crn_yes_no",
                                        reasonLabel: String = "messages__company__cya__crn_no_reason",
                                        changeHasCrn: String = "messages__visuallyhidden__establisher__crn_yes_no",
                                        changeCrn: String = "messages__visuallyhidden__establisher__crn",
                                        changeNoCrn: String = "messages__visuallyhidden__establisher__crn_no"
                                      ) {

  def apply()(implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(
            AnswerRow(label, Seq(s"${CompanyRegistrationNumber.Yes}"), answerIsMessageKey = true,
              Some(Link("site.change", changeUrl, Some(changeHasCrn)))),
            AnswerRow("messages__common__crn", Seq(s"$crn"), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeCrn))))
          )
          case Some(CompanyRegistrationNumber.No(reason)) => Seq(
            AnswerRow(label, Seq(s"${CompanyRegistrationNumber.No}"), answerIsMessageKey = true,
              Some(Link("site.change", changeUrl, Some(changeHasCrn)))),
            AnswerRow(reasonLabel, Seq(s"$reason"), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeNoCrn))))
          )
          case _ => Seq.empty[AnswerRow]
        }
      }

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(
            AnswerRow("messages__common__crn", Seq(s"$crn"), answerIsMessageKey = false, None)
          )
          case Some(CompanyRegistrationNumber.No(_)) => Seq(
            AnswerRow("messages__common__crn", Seq("site.not_entered"), answerIsMessageKey = true,
              Some(Link("site.add", changeUrl, Some(changeCrn))))
          )
          case _ => Seq.empty[AnswerRow]
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
              Some(Link("site.change", changeUrl,
                Some(hiddenLabel.fold(s"messages__visuallyhidden__${id.toString}")(customHiddenLabel => customHiddenLabel))))
            ))
        }.getOrElse(Seq.empty[AnswerRow])

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)
    }
  }
}

case class AddressYearsCYA[I <: TypedIdentifier[AddressYears]](label: String = "messages__establisher_address_years__title",
                                                               changeAddressYears: String = "messages__visuallyhidden__common__address_years") {

  def apply()(implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map(
          addressYears =>
            Seq(AnswerRow(
              label = label,
              answer = Seq(s"messages__common__$addressYears"),
              answerIsMessageKey = true,
              changeUrl = Some(Link("site.change", changeUrl, Some(changeAddressYears)))
            ))
        ).getOrElse(Seq.empty[AnswerRow])

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = Nil
    }
  }

}

case class AddressCYA[I <: TypedIdentifier[Address]](
                                                      label: String = "messages__common__cya__address",
                                                      changeAddress: String = "messages__visuallyhidden__common__address"
                                                    ) {

  def apply()(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            userAnswers.addressAnswer(address),
            answerIsMessageKey = false,
            Some(Link("site.change", changeUrl,
              Some(changeAddress)))
          ))
        }.getOrElse(Seq.empty[AnswerRow])
      }

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        row(id)(changeUrl, userAnswers)
    }
  }

}

case class PreviousAddressCYA[I <: TypedIdentifier[Address]](label: String,
                                                             changeAddress: String,
                                                             isNew: Option[Boolean],
                                                             addressYear: Option[AddressYears]
                                                            ) {

  def apply()(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        isNew match {
          case Some(true) =>
            AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id) match {
              case Some(_) => row(id)(changeUrl, userAnswers)
              case _ =>
                addressYear match {
                  case Some(UnderAYear) => Seq(AnswerRow(label,
                    Seq("site.not_entered"),
                    answerIsMessageKey = true,
                    Some(Link("site.add", changeUrl, Some(changeAddress)))))
                  case _ => Seq.empty[AnswerRow]
                }
            }
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
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) => Seq(
            AnswerRow(label, Seq(s"${UniqueTaxReference.Yes}"), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeHasUtr)))),
            AnswerRow(utrLabel, Seq(utr), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeUtr))))
          )
          case Some(UniqueTaxReference.No(reason)) => Seq(
            AnswerRow(label, Seq(s"${UniqueTaxReference.No}"), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeHasUtr)))),
            AnswerRow(reasonLabel, Seq(reason), answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(changeNoUtr))))
          )
          case _ => Seq.empty[AnswerRow]
        }

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) => Seq(AnswerRow(utrLabel, Seq(utr), answerIsMessageKey = false, None))
          case Some(UniqueTaxReference.No(_)) => Seq(AnswerRow(utrLabel, Seq("site.not_entered"), answerIsMessageKey = true, None))
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
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(DeclarationDormant.Yes) => Seq(
            AnswerRow(
              label,
              Seq("site.yes"),
              answerIsMessageKey = true,
              Some(Link("site.change", changeUrl,
                Some(changeIsDormant)))
            )
          )
          case Some(DeclarationDormant.No) => Seq(
            AnswerRow(
              label,
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(Link("site.change", changeUrl,
                Some(changeIsDormant)))
            ))
          case _ => Seq.empty[AnswerRow]
        }

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = Nil
    }
  }

}

case class CompanyDetailsCYA[I <: TypedIdentifier[CompanyDetails]](
                                                                    nameLabel: String = "messages__common__cya__name",
                                                                    hiddenNameLabel: String = "messages__visuallyhidden__common__name") {

  def apply()(implicit rds: Reads[CompanyDetails], messages: Messages): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {

      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map { companyDetails =>
          Seq(AnswerRow(nameLabel, Seq(s"${companyDetails.companyName}"), answerIsMessageKey = false,
            Some(Link("site.change", changeUrl, Some(Message(hiddenNameLabel, companyDetails.companyName))))
          ))
        }.getOrElse(Seq.empty[AnswerRow])

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map { companyDetails =>
          Seq(AnswerRow(nameLabel, Seq(s"${companyDetails.companyName}"), answerIsMessageKey = false, None))
        }.getOrElse(Seq.empty[AnswerRow])
    }
  }

}

case class ReferenceValueCYA[I <: TypedIdentifier[ReferenceValue]](
                                                                    nameLabel: String = "messages__common__cya__name",
                                                                    hiddenNameLabel: String = "messages__visuallyhidden__common__name") {

  def apply()(implicit rds: Reads[ReferenceValue], messages: Messages): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {

      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map { reference =>
          Seq(AnswerRow(nameLabel, Seq(s"${reference.value}"), answerIsMessageKey = false,
            Some(Link("site.change", changeUrl, Some(hiddenNameLabel)))
          ))
        }.getOrElse(Seq.empty[AnswerRow])

      override def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(reference) if !reference.isEditable =>
            Seq(AnswerRow(nameLabel, Seq(reference.value), answerIsMessageKey = false, None))
          case Some(_) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            Seq(AnswerRow(nameLabel, Seq("site.not_entered"), answerIsMessageKey = true,
              Some(Link("site.add", changeUrl, Some(hiddenNameLabel)))))
        }
    }
  }

}
