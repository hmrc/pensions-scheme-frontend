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

import models.details.{PsaSchemeDetails, _}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.FakeCountryOptions

import scala.language.implicitConversions

class PsaSchemeDetailsMasterSectionSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val individualInfoRows: IndividualInfoRows[IndividualInfo] = new IndividualInfoRows[IndividualInfo](FakeCountryOptions())
  val companyDetailsRows: CompanyDetailsRows[CompanyDetails] = new CompanyDetailsRows[CompanyDetails](FakeCountryOptions())
  val partnershipDetailsRows: PartnershipDetailsRows[PartnershipDetails] = new PartnershipDetailsRows[PartnershipDetails](FakeCountryOptions())

  val schemeDetailsRows: SchemeDetailsSection[SchemeDetails] = new SchemeDetailsSection[SchemeDetails](FakeCountryOptions())
  val establisherInfoRows: EstablisherInfoSection = new EstablisherInfoSection(individualInfoRows, companyDetailsRows, partnershipDetailsRows)
  val trusteeInfoRows: TrusteeInfoSection = new TrusteeInfoSection(individualInfoRows, companyDetailsRows, partnershipDetailsRows)

  val psaSchemeDetailsMasterSection = PsaSchemeDetailsMasterSection(schemeDetailsRows, establisherInfoRows, trusteeInfoRows)

  "PsaSchemeDetailsMasterSection" must {

    "produce section of correct data" when {

      "all data present for scheme details" in {

        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, None, None, Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection)
        )
      }

      "all data present for scheme details and establishers" in {

        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, Some(establisherDetails), None, Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection, establisherMasterSection)
        )
      }

      "all data present for scheme details and trutees" in {

        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, None, Some(trusteeDetails), Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection, trsuteeMasterSection)
        )
      }

      "all data present for scheme details, establishers and trutees" in {

        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, Some(establisherDetails), Some(trusteeDetails), Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection, establisherMasterSection, trsuteeMasterSection)
        )
      }
    }
  }
}
