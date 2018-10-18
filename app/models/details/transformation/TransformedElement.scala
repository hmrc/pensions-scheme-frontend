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

package models.details.transformation

import models.details._
import utils.CountryOptions
import viewmodels.{AnswerRow, SuperSection}

import scala.language.implicitConversions

trait TransformedElement[I] {

  val entityType: String = "individual"

  def transformSuperSection(data: I): SuperSection

  def transformRows(data: I): Seq[AnswerRow]

  def transformRow(label: String,
                   answer: Seq[String],
                   answerIsMessageKey: Boolean = false,
                   changeUrl: Option[String] = None): AnswerRow = {

    AnswerRow(label, answer, answerIsMessageKey, changeUrl)
  }

  def fullName(data: IndividualName): String = data.middleName match {

    case Some(middle) => s"${data.firstName} $middle ${data.lastName}"
    case _ => s"${data.firstName} ${data.lastName}"
  }

  protected def getCountry(countryOptions: CountryOptions, countryName: String): String = {

    countryOptions.options.find(_.value == countryName).map(_.label).getOrElse(countryName)
  }

  protected def addressAnswer(countryOptions: CountryOptions, address: CorrespondenceAddress): Seq[String] = {

    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postalCode.map(postCode => s"$postCode,"),
      Some(getCountry(countryOptions, address.countryCode))
    ).flatten
  }

  protected def yesNo(flag: Boolean): String = {

    if (flag) "site.yes" else "site.no"
  }

  protected def utrRows(utrStr: Option[String]): Seq[AnswerRow] = {

    utrStr.map { utr =>
      transformRow(label = s"messages__psaSchemeDetails__${entityType}_utr", answer = Seq(utr))
    }.toSeq
  }

  protected def vatRegistrationRows(vatRegistrationStr: Option[String]): Seq[AnswerRow] = {

    vatRegistrationStr.map { vatRegistration =>
      transformRow(label = s"messages__psaSchemeDetails__${entityType}_vat", answer = Seq(vatRegistration))
    }.toSeq
  }

  protected def payeRefRows(payeRefStr: Option[String]): Seq[AnswerRow] = {

    payeRefStr.map { payeRef =>
      transformRow(label = s"messages__psaSchemeDetails__${entityType}_paye", answer = Seq(payeRef))
    }.toSeq
  }

  protected def crnRows(crnStr: Option[String]): Seq[AnswerRow] = {

    crnStr.map { crn =>
      transformRow(label = s"messages__psaSchemeDetails__${entityType}_crn", answer = Seq(crn))
    }.toSeq
  }

  protected def addressRows(countryOptions: CountryOptions, address: CorrespondenceAddress): Seq[AnswerRow] = {

    Seq(transformRow(label = s"messages__psaSchemeDetails__${entityType}_address", answer = addressAnswer(countryOptions, address)))
  }

  protected def contactRows(contact: IndividualContactDetails): Seq[AnswerRow] = {

    Seq(transformRow(label = s"messages__psaSchemeDetails__${entityType}_email", answer = Seq(contact.email)),
      transformRow(label = s"messages__psaSchemeDetails__${entityType}_phone", answer = Seq(contact.telephone)))
  }

  protected def previousAddressRows(countryOptions: CountryOptions, previousAddress: Option[PreviousAddressInfo]): Seq[AnswerRow] = {

    previousAddress.map {
      address =>
        Seq(transformRow(label = s"messages__psaSchemeDetails__${entityType}_less_than_12months", answer = Seq(
          if (address.isPreviousAddressLast12Month) {
            "companyAddressYears.under_a_year"
          } else {
            "companyAddressYears.over_a_year"
          }),
          answerIsMessageKey = true)) ++
          address.previousAddress.map {
            previousAddress =>
              transformRow(label = s"messages__psaSchemeDetails__${entityType}_previous_address", answer = addressAnswer(countryOptions, previousAddress))
          }
    }.toSeq.flatten
  }

}
