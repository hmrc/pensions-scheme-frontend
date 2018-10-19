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
import viewmodels.{AnswerSection, MasterSection, SuperSection}

import scala.language.implicitConversions

class SchemeDetailsMasterSectionSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val individualInfoRows: IndividualInfoRows[IndividualInfo] = new IndividualInfoRows[IndividualInfo](FakeCountryOptions())
  val companyDetailsRows: CompanyDetailsRows[CompanyDetails] = new CompanyDetailsRows[CompanyDetails](FakeCountryOptions())
  val partnershipDetailsRows: PartnershipDetailsRows[PartnershipDetails] = new PartnershipDetailsRows[PartnershipDetails](FakeCountryOptions())

  val schemeDetailsRows: SchemeDetailsSection[SchemeDetails] = new SchemeDetailsSection[SchemeDetails](FakeCountryOptions())

  val psaSchemeDetailsMasterSection = new SchemeDetailsMasterSection(schemeDetailsRows, individualInfoRows, companyDetailsRows, partnershipDetailsRows)

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

  "EstablisherInfoSection" must {

    "produce section of correct data" when {

      "all data present for individuals" in {

        val establisherDetails = EstablisherInfo(Seq(individuals), Seq(), Seq())
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, Some(establisherDetails), None, Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection,
            MasterSection(Some("messages__psaSchemeDetails__establishers"),
              Seq(SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows))))
            )
          )
        )
      }

      "all data present for companies" in {

        val establisherDetails = EstablisherInfo(Seq(), Seq(companyDetails), Seq())
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, Some(establisherDetails), None, Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection, MasterSection(Some("messages__psaSchemeDetails__establishers"),
            Seq(
              SuperSection(Some("abc organisation"), Seq(AnswerSection(None, companyAnswerRows))),
              SuperSection(Some("messages__psaSchemeDetails__director_details"),
                Seq(individualAnswerRow))
            )
          )
          )
        )
      }

      "all data present for partnership" in {

        val establisherDetails = EstablisherInfo(Seq(), Seq(), Seq(partnershipDetails))
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, Some(establisherDetails), None, Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection,
            MasterSection(Some("messages__psaSchemeDetails__establishers"),
              Seq(
                SuperSection(Some("abc partnership"), Seq(AnswerSection(None, partnershipAnswerRows))),
                SuperSection(Some("messages__psaSchemeDetails__partner_details"), Seq(individualAnswerRow))
              )
            )
          )
        )
      }

      "all individual, companies and partnership data present" in {

        val establisherDetails = EstablisherInfo(Seq(individuals), Seq(companyDetails), Seq(partnershipDetails))
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, Some(establisherDetails), None, Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection, establisherMasterSection)
        )
      }
    }
  }

  "TrusteeInfoSection" must {

    "produce section of correct data" when {

      "all data present for individuals" in {

        val trusteeDetails = TrusteeInfo(Seq(individuals), Seq(), Seq())
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, None, Some(trusteeDetails), Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection,
            MasterSection(Some("messages__psaSchemeDetails__trustees"),
              Seq(SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows))))
            )
          )
        )
      }

      "all data present for companies" in {

        val trusteeDetails = TrusteeInfo(Seq(), Seq(trusteeCompanyDetails), Seq())
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, None, Some(trusteeDetails), Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection,
            MasterSection(Some("messages__psaSchemeDetails__trustees"),
              Seq(
                SuperSection(Some("abc organisation"), Seq(AnswerSection(None, companyAnswerRows)))
              )
            )
          )
        )
      }

      "all data present for partnership" in {

        val trusteeDetails = TrusteeInfo(Seq(), Seq(), Seq(trusteePartnershipDetails))
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, None, Some(trusteeDetails), Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection,
            MasterSection(Some("messages__psaSchemeDetails__trustees"),
              Seq(
                SuperSection(Some("abc partnership"), Seq(AnswerSection(None, partnershipAnswerRows)))
              )
            )
          )
        )
      }

      "all individual, companies and partnership data present" in {

        val trusteeDetails = TrusteeInfo(Seq(individuals), Seq(trusteeCompanyDetails), Seq(trusteePartnershipDetails))
        val psaSchemeDetailsSection = PsaSchemeDetails(minimumSchemeDetails, None, Some(trusteeDetails), Nil)

        psaSchemeDetailsMasterSection.transformMasterSection(psaSchemeDetailsSection) must equal(

          Seq(individualMasterSection, trsuteeMasterSection)
        )
      }
    }
  }
}
