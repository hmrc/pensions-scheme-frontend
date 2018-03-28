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
import models.address.Address
import models.register.establishers.individual.UniqueTaxReference
import models.requests.DataRequest
import models.{AddressYears, CompanyDetails, CompanyRegistrationNumber}
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import viewmodels.AnswerRow

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(string),
              answerIsMessageKey = false,
              changeUrl
            ))
        }.getOrElse(Seq.empty)
    }

  implicit def companyDetails[I <: TypedIdentifier[CompanyDetails]](implicit rds: Reads[CompanyDetails]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        val nameLabel = "messages__common__cya__name"
        val vatLabel = "messages__company__cya__vat"
        val payeLabel = "messages__company__cya__paye_ern"

        userAnswers.get(id).map {
          companyDetails =>

            val nameRow = AnswerRow(
              nameLabel,
              Seq(s"${companyDetails.companyName}"),
              false,
              changeUrl
            )

            val withVat = companyDetails.vatNumber.fold(Seq(nameRow)){ vat =>
              Seq(nameRow, AnswerRow(
                vatLabel,
                Seq(s"$vat"),
                false,
                changeUrl
              ))
            }

            companyDetails.payeNumber.fold(withVat){ paye =>
              withVat :+ AnswerRow(
                payeLabel,
                Seq(s"$paye"),
                false,
                changeUrl
              )
            }

        }.getOrElse(Seq.empty[AnswerRow])
      }
    }

  implicit def companyRegistrationNumber[I <: TypedIdentifier[CompanyRegistrationNumber]](implicit rds: Reads[CompanyRegistrationNumber]): CheckYourAnswers[I] =
    new CheckYourAnswers[I]{
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        val crnLabel = "messages__company__cya__crn_yes_no"

        userAnswers.get(id) match {
          case Some(CompanyRegistrationNumber.Yes(crn)) => Seq(
            AnswerRow(
              crnLabel,
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
              crnLabel,
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

  implicit def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]](implicit rds: Reads[UniqueTaxReference]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        val utrLabel = "messages__establisher_individual_utr_question_cya_label"

        userAnswers.get(id) match {
          case Some(UniqueTaxReference.Yes(utr)) => Seq(
            AnswerRow(
              utrLabel,
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
              utrLabel,
              Seq(s"${UniqueTaxReference.No}"),
              false,changeUrl
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
    }

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        def addressAnswer(address: Address): Seq[String] = {
          val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
          Seq(
            Some(s"${address.addressLine1},"),
            Some(s"${address.addressLine2},"),
            address.addressLine3.map(line3 => s"$line3,"),
            address.addressLine4.map(line4 => s"$line4,"),
            address.postCode.map(postCode => s"$postCode,"),
            Some(country)
          ).flatten
        }

        userAnswers.get(id).map{ address =>
          Seq(AnswerRow(
            "messages__establisher_individual_address_cya_label",
            addressAnswer(address),
            false,changeUrl
          ))
        }.getOrElse(Seq.empty[AnswerRow])
      }
    }

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = userAnswers.get(id).map( addressYears =>
        Seq(AnswerRow(
        "messages__establisher_individual_address_years_cya_label",
        Seq(s"messages__common__$addressYears"),
        true,
        changeUrl
      ))).getOrElse(Seq.empty[AnswerRow])
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