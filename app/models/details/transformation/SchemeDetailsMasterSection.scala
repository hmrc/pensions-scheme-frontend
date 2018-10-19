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
import viewmodels.{MasterSection, SuperSection}

import scala.language.implicitConversions

class SchemeDetailsMasterSection @Inject()(schemeDetails: SchemeDetailsSection[SchemeDetails],
                                           individualInfoRows: IndividualInfoRows[IndividualInfo],
                                           companyDetailsRows: CompanyDetailsRows[CompanyDetails],
                                           partnershipDetailsRows: PartnershipDetailsRows[PartnershipDetails]) {

  def transformMasterSection(data: PsaSchemeDetails): Seq[MasterSection] = {

    val schemeDetailsSection = Seq(MasterSection(None, Seq(schemeDetails.transformSuperSection(data.schemeDetails))))

    val establisherDetailsSection = data.establisherDetails.map {
      establisher =>
        transformEstablisherMasterSection(establisher)
    }.toSeq

    val trusteeDetailsSection = data.trusteeDetails.map {
      trustee =>
        transformTrusteeMasterSection(trustee)
    }.toSeq

    schemeDetailsSection ++ establisherDetailsSection ++ trusteeDetailsSection

  }

  def transformEstablisherMasterSection(data: EstablisherInfo): MasterSection = {

    val individuals = individualSuperSection(data.individual)

    val companies = companySuperSection(data.company)

    val partnerships = partnershipSuperSection(data.partnership)

    MasterSection(Some("messages__psaSchemeDetails__establishers"), individuals ++ companies ++ partnerships)

  }

  def transformTrusteeMasterSection(data: TrusteeInfo): MasterSection = {

    val individuals = individualSuperSection(data.individual)

    val companies = companySuperSection(data.company)

    val partnerships = partnershipSuperSection(data.partnership)

    MasterSection(Some("messages__psaSchemeDetails__trustees"), individuals ++ companies ++ partnerships)

  }

  private def individualSuperSection(individual: Seq[IndividualInfo]): Seq[SuperSection] = {

    individual.map {
      indv =>
        individualInfoRows.transformSuperSection(indv)
    }
  }

  private def companySuperSection(company: Seq[CompanyDetails]): Seq[SuperSection] = {

    company.flatMap {
      comp =>
        Seq(companyDetailsRows.transformSuperSection(comp)) ++
        comp.directorsDetails.map {
          indv =>
            individualInfoRows.transformSuperSection(indv,
              Some("messages__psaSchemeDetails__director_details"))
        }
    }
  }

  private def partnershipSuperSection(partnership: Seq[PartnershipDetails]): Seq[SuperSection] = {

    partnership.flatMap {
      partner =>
        Seq(partnershipDetailsRows.transformSuperSection(partner)) ++
        partner.partnerDetails.map { indv =>
          individualInfoRows.transformSuperSection(indv,
            Some("messages__psaSchemeDetails__partner_details"))
        }
    }
  }
}
