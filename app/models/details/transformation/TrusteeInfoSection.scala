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

class TrusteeInfoSection @Inject()(individualInfoRows :IndividualInfoRows[IndividualInfo],
                                            companyDetailsRows : CompanyDetailsRows[CompanyDetails],
                                            partnershipDetailsRows : PartnershipDetailsRows[PartnershipDetails]) {

  def transformMasterSection(data: TrusteeInfo): MasterSection = {

    val individuals = data.individual.map {
      indv =>
        individualInfoRows.transformSuperSection(indv)
    }

    val companies = data.company.map {
      comp =>
        companyDetailsRows.transformSuperSection(comp)
    }

    val partnerships = data.partnership.map {
      partner =>
        partnershipDetailsRows.transformSuperSection(partner)
    }

    MasterSection(Some("messages__psaSchemeDetails__trustees"), individuals ++ companies ++ partnerships)

  }
}
