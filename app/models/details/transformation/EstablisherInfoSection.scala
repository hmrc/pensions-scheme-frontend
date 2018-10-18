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
import viewmodels.MasterSection

import scala.language.implicitConversions

case class EstablisherInfoSection @Inject()(individualInfoRows :IndividualInfoRows[IndividualInfo],
                                            companyDetailsRows : CompanyDetailsRows[CompanyDetails],
                                            partnershipDetailsRows : PartnershipDetailsRows[PartnershipDetails]) {

  def transformMasterSection(data: EstablisherInfo): MasterSection = {

    val individuals = data.individual.map {
      indv =>
        individualInfoRows.transformSuperSection(indv)
    }

    val companies = data.company.flatMap {
      comp =>
        Seq(companyDetailsRows.transformSuperSection(comp)) ++
          comp.directorsDetails.map {
            indv =>
              individualInfoRows.transformSuperSection(indv,
                Some("messages__psaSchemeDetails__director_details"))
          }
    }


    val partnerships = data.partnership.flatMap {
      partner =>
        Seq(partnershipDetailsRows.transformSuperSection(partner)) ++
          partner.partnerDetails.map { indv =>
            individualInfoRows.transformSuperSection(indv,
              Some("messages__psaSchemeDetails__partner_details"))
          }
    }

    MasterSection(Some("messages__psaSchemeDetails__establishers"), individuals ++ companies ++ partnerships)

  }

}
