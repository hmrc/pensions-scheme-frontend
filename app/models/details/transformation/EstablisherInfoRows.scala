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
import viewmodels.{AnswerRow, AnswerSection, MasterSection, SuperSection}

import scala.language.implicitConversions

case class EstablisherInfoRows@Inject()(countryOptions : CountryOptions) {

  def transformSuperSection(data: EstablisherInfo): MasterSection = {

    val individuals = data.individual.map{ indv =>
      IndividualInfoRows(countryOptions).transformSuperSection(indv, None)
    }.toList

    val companiess = data.company.map{ comp =>
      Seq(CompanyDetailsRows(countryOptions).transformSuperSection(comp)) ++
      comp.directorsDetails.map{ indv =>
        IndividualInfoRows(countryOptions).transformSuperSection(indv, Some("messages__psaSchemeDetails__director_details"))
      }.toList
    }.flatten


    val partnerships = data.partnership.map{ partner =>
      Seq(PartnershipDetailsRows(countryOptions).transformSuperSection(partner))++
        partner.partnerDetails.map{ indv =>
        IndividualInfoRows(countryOptions).transformSuperSection(indv, Some("messages__psaSchemeDetails__partner_details"))
      }.toList
    }.flatten


    MasterSection(Some("messages__psaSchemeDetails__establishers"), individuals ++ companiess ++ partnerships)

  }

}
