/*
 * Copyright 2020 HM Revenue & Customs
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

import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.partnership.partner.PartnerNameId
import identifiers.{EstablishedCountryId, TypedIdentifier}
import models._
import models.address.Address
import models.register._
import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]

  def updateRow(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

trait CheckYourAnswersDirectors[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  private def directorName(establisherIndex:Int, directorIndex:Int, ua:UserAnswers)(implicit messages:Messages):String =
    ua.get(DirectorNameId(establisherIndex, directorIndex)).fold(messages("messages__theDirector"))(_.fullName)

  protected def dynamicMessage(establisherIndex: Int, directorIndex: Int, ua:UserAnswers, messageKey:String)(implicit messages:Messages) =
    messages(messageKey, directorName(establisherIndex, directorIndex, ua))

}

trait CheckYourAnswersPartners[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  private def partnerName(establisherIndex:Int, partnerIndex:Int, ua:UserAnswers)(implicit messages:Messages):String =
    ua.get(PartnerNameId(establisherIndex, partnerIndex)).fold(messages("messages__thePartner"))(_.fullName)

  protected def dynamicMessage(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers, messageKey:String)(implicit messages:Messages) =
    messages(messageKey, partnerName(establisherIndex, partnerIndex, ua))

}

trait CheckYourAnswersCompany[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  private def companyName(establisherIndex:Int, ua:UserAnswers)(implicit messages:Messages):String =
    ua.get(CompanyDetailsId(establisherIndex)).fold(messages("messages__theCompany"))(_.companyName)

  protected def dynamicMessage(establisherIndex: Int, ua:UserAnswers, messageKey:String)(implicit messages:Messages) =
    messages(messageKey, companyName(establisherIndex, ua))

}

object CheckYourAnswers {

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] = AddressYearsCYA()()

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = AddressCYA()()

  implicit def dormant[I <: TypedIdentifier[DeclarationDormant]](implicit rds: Reads[DeclarationDormant]): CheckYourAnswers[I] = IsDormantCYA()()

  implicit def companyDetails[I <: TypedIdentifier[CompanyDetails]]
  (implicit rds: Reads[CompanyDetails], messages: Messages): CheckYourAnswers[I] = CompanyDetailsCYA()()

  implicit def reference[I <: TypedIdentifier[ReferenceValue]]
  (implicit rds: Reads[ReferenceValue], messages: Messages): CheckYourAnswers[I] = ReferenceValueCYA()()

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String], countryOptions: CountryOptions): CheckYourAnswers[I] = StringCYA()()

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] = BooleanCYA()()

  implicit def members[I <: TypedIdentifier[Members]](implicit rds: Reads[Members]): CheckYourAnswers[I] = MembersCYA()()

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

  case class PartnershipDetailsCYA[I <: TypedIdentifier[PartnershipDetails]]() {

    def apply()(implicit rds: Reads[PartnershipDetails], messages: Messages): CheckYourAnswers[I] = {
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

case class BankDetailsCYA[I <: TypedIdentifier[BankAccountDetails]](label: Option[String] = None, hiddenLabel: Option[String] = None) {

  def apply()(implicit rds: Reads[BankAccountDetails], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          bankDetails =>
            Seq(AnswerRow(
              label.fold(s"${id.toString}.checkYourAnswersLabel")(customLabel => customLabel),
              Seq(
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
                                                             isThisPreviousAddress: Option[Boolean]
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
                isThisPreviousAddress match {
                  case Some(false) => Seq(AnswerRow(label,
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
