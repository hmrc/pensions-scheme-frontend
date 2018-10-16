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

package models.details.view

import javax.inject.Inject
import models.details.{CorrespondenceAddress, SchemeDetails}
import models.register.SchemeType
import utils.CountryOptions
import viewmodels.AnswerRow

import scala.language.implicitConversions

//scalastyle:off method.length
case class SchemeDetailsRows[I <: SchemeDetails] @Inject()(countryOptions: CountryOptions) extends DetailAnswerRow[I] {

  override def row(data: I): Seq[AnswerRow] = {
    SchemeType.getSchemeType(data.typeOfScheme, data.isMasterTrust).map {
      schemeType =>
        AnswerRow(
          "messages__psaSchemeDetails__scheme_type",
          Seq(schemeType),
          answerIsMessageKey = false,
          None
        )
    }.toList ++ Seq(
      AnswerRow(
        "messages__psaSchemeDetails__country_established",
        Seq(s"${getCountry(data.country)}"),
        answerIsMessageKey = false,
        None
      ),
      AnswerRow(
        "messages__psaSchemeDetails__current_scheme_members",
        Seq(s"${data.members.current}"),
        answerIsMessageKey = false,
        None
      ),
      AnswerRow(
        "messages__psaSchemeDetails__future_scheme_members",
        Seq(s"${data.members.future}"),
        answerIsMessageKey = false,
        None
      ),
      AnswerRow(
        "messages__psaSchemeDetails__is_investment_regulated",
        Seq(getYesNo(data.isInvestmentRegulated)),
        answerIsMessageKey = true,
        None
      ),
      AnswerRow(
        "messages__psaSchemeDetails__is_occupational",
        Seq(getYesNo(data.isOccupational)),
        answerIsMessageKey = true,
        None
      ),
      AnswerRow(
        "messages__psaSchemeDetails__benefits",
        Seq(s"${data.benefits}"),
        answerIsMessageKey = false,
        None
      ),
      AnswerRow(
        "messages__psaSchemeDetails__are_benefits_secured",
        Seq(getYesNo(data.areBenefitsSecured)),
        answerIsMessageKey = true,
        None
      )
    ) ++ data.insuranceCompany.map {
      company =>
        company.name.map { name =>
          AnswerRow(
            "messages__psaSchemeDetails__insurance_company_name",
            Seq(name),
            answerIsMessageKey = false,
            None
          )
        }.toList ++
          company.policyNumber.map { policyNumber =>
            AnswerRow(
              "messages__psaSchemeDetails__policy_number",
              Seq(policyNumber),
              answerIsMessageKey = false,
              None
            )
          }.toList ++
          company.address.map { address =>
            AnswerRow(
              "messages__psaSchemeDetails__insurance_company_address",
              addressAnswer(address),
              answerIsMessageKey = false,
              None
            )
          }
    }.toList.flatten
  }


  private def getCountry(countryName: String): String = {
    countryOptions.options.find(_.value == countryName).map(_.label).getOrElse(countryName)
  }

  private def addressAnswer(address: CorrespondenceAddress): Seq[String] = {
    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postalCode.map(postCode => s"$postCode,"),
      Some(getCountry(address.countryCode))
    ).flatten
  }

  private def getYesNo(flag : Boolean): String ={
    if (flag) "site.yes" else "site.no"
  }

}
