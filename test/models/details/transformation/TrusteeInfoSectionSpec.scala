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
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.FakeCountryOptions
import viewmodels.{AnswerSection, MasterSection, SuperSection}

import scala.language.implicitConversions

class TrusteeInfoSectionSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val individualInfoRows: IndividualInfoRows[IndividualInfo] = new IndividualInfoRows[IndividualInfo](FakeCountryOptions())
  val companyDetailsRows: CompanyDetailsRows[CompanyDetails] = new CompanyDetailsRows[CompanyDetails](FakeCountryOptions())
  val partnershipDetailsRows: PartnershipDetailsRows[PartnershipDetails] = new PartnershipDetailsRows[PartnershipDetails](FakeCountryOptions())

  val trusteeInfoRows: TrusteeInfoSection = new TrusteeInfoSection(individualInfoRows, companyDetailsRows, partnershipDetailsRows)

  val individualAnswerRow = AnswerSection(Some("fName mName lName"), indidualAnswerRows)

  "TrusteeInfoSection" must {

    "produce section of correct data" when {

      "all data present for individuals" in {

        val trusteeDetails = TrusteeInfo(Seq(individuals), Seq(), Seq())

        trusteeInfoRows.transformMasterSection(trusteeDetails) must equal(
          MasterSection(Some("messages__psaSchemeDetails__trustees"),
            Seq(SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows))))
          )
        )
      }

      "all data present for companies" in {

        val trusteeDetails = TrusteeInfo(Seq(), Seq(trusteeCompanyDetails), Seq())

        trusteeInfoRows.transformMasterSection(trusteeDetails) must equal(

          MasterSection(Some("messages__psaSchemeDetails__trustees"),
            Seq(
              SuperSection(Some("abc organisation"), Seq(AnswerSection(None, companyAnswerRows)))
            )
          )
        )
      }

      "all data present for partnership" in {

        val trusteeDetails = TrusteeInfo(Seq(), Seq(), Seq(trusteePartnershipDetails))

        trusteeInfoRows.transformMasterSection(trusteeDetails) must equal(
          MasterSection(Some("messages__psaSchemeDetails__trustees"),
            Seq(
              SuperSection(Some("abc partnership"), Seq(AnswerSection(None, partnershipAnswerRows)))
            )
          )
        )
      }

      "all individual, companies and partnership data present" in {

        val trusteeDetails = TrusteeInfo(Seq(individuals), Seq(trusteeCompanyDetails), Seq(trusteePartnershipDetails))

        trusteeInfoRows.transformMasterSection(trusteeDetails) must equal(
          MasterSection(Some("messages__psaSchemeDetails__trustees"),
            Seq(
              SuperSection(Some("fName mName lName"), Seq(AnswerSection(None, indidualAnswerRows))),
              SuperSection(Some("abc organisation"), Seq(AnswerSection(None, companyAnswerRows))),
              SuperSection(Some("abc partnership"), Seq(AnswerSection(None, partnershipAnswerRows)))
            )
          )
        )
      }
    }
  }
}
