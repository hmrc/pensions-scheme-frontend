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

import javax.inject.Inject
import models.details._
import org.joda.time.LocalDate
import utils.{CountryOptions, DateHelper}
import viewmodels.{AnswerRow, AnswerSection, SuperSection}

import scala.language.implicitConversions

class IndividualInfoRows[I <: IndividualInfo] @Inject()(countryOptions: CountryOptions) extends TransformedElement[I] {

  override val entityType = "individual"

  def transformSuperSection(data: I, heading : Option[String]): SuperSection = {

    val superHeading = if(heading.isEmpty){Some(fullName(data.personalDetails.name))} else heading

    val answerHeading = if(heading.isEmpty){None} else {Some(fullName(data.personalDetails.name))}

    SuperSection(superHeading, Seq(AnswerSection(answerHeading, transformRows(data))))
  }

  def transformAnswerSection(data: I): AnswerSection = {

    AnswerSection(Some(fullName(data.personalDetails.name)), transformRows(data))
  }

  override def transformSuperSection(data: I): SuperSection = {

    SuperSection(Some(fullName(data.personalDetails.name)), Seq(AnswerSection(None, transformRows(data))))
  }

  override def transformRows(data: I): Seq[AnswerRow] = {

    dateOfBirthRows(data.personalDetails) ++
      ninoRows(data.nino) ++
      utrRows(data.utr) ++
      addressRows(countryOptions, data.address) ++
      previousAddressRows(countryOptions, Some(data.previousAddress)) ++
      contactRows(data.contact)
  }

  private def dateOfBirthRows(personalDetails: PersonalInfo): Seq[AnswerRow]  = {

    Seq(transformRow(label = "messages__psaSchemeDetails__individual_date_of_birth", answer = Seq(
      DateHelper.formatDate(new LocalDate(personalDetails.dateOfBirth)))))
  }

  private def ninoRows(ninoStr: Option[String]): Seq[AnswerRow]  = {

    ninoStr.map{ nino =>
      transformRow(label = "messages__psaSchemeDetails__individual_nino", answer = Seq(nino))
    }.toSeq
  }
}
