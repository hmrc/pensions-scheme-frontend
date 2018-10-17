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
import viewmodels.{AnswerRow, AnswerSection, MasterSection, SuperSection}

import scala.language.implicitConversions

class EstablisherInfoRowsSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val establisherInfoRows: EstablisherInfoRows = EstablisherInfoRows(FakeCountryOptions())

  "EstablisherInfoRows" must {

    "produce section of correct data" when {

      "all data present for individuals" in {

        val establisherDetails = EstablisherInfo(Seq(individuals), Seq(), Seq())

        establisherInfoRows.transformSuperSection(establisherDetails) must equal(
          MasterSection(Some("messages__psaSchemeDetails__establishers"),
            Seq(SuperSection(Some("fName mName lName"),
              Seq(AnswerSection(None, Seq(
                AnswerRow("messages__psaSchemeDetails__individual_date_of_birth", Seq("29 March 1955"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__individual_nino", Seq("AA999999A"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__individual_utr", Seq("1234567892"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__individual_address", Seq(
                  "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__individual_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
                AnswerRow("messages__psaSchemeDetails__individual_previous_address", Seq(
                  "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__individual_email", Seq("test@test.com"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__individual_phone", Seq("07592113"), answerIsMessageKey = false, None)))))
            )
          )
        )
      }

      "all data present for companies" in {

        val establisherDetails = EstablisherInfo(Seq(), Seq(companyDetails), Seq())

        establisherInfoRows.transformSuperSection(establisherDetails) must equal(

          MasterSection(Some("messages__psaSchemeDetails__establishers"),
            Seq(SuperSection(Some("abc organisation"),
              Seq(AnswerSection(None, Seq(
                AnswerRow("messages__psaSchemeDetails__company_vat", Seq("789770000"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_paye", Seq("9999"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_crn", Seq("AA999999A"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_utr", Seq("7897700000"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_address", Seq(
                  "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
                AnswerRow("messages__psaSchemeDetails__company_previous_address", Seq(
                  "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_email", Seq("test@test.com"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__company_phone", Seq("07592113"), answerIsMessageKey = false, None)
              )))),
              SuperSection(Some("messages__psaSchemeDetails__director_details"),
                Seq(AnswerSection(Some("fName mName lName"), Seq(
                  AnswerRow("messages__psaSchemeDetails__individual_date_of_birth", Seq("29 March 1955"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_nino", Seq("AA999999A"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_utr", Seq("1234567892"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_address", Seq(
                    "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
                  AnswerRow("messages__psaSchemeDetails__individual_previous_address", Seq(
                    "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_email", Seq("test@test.com"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_phone", Seq("07592113"), answerIsMessageKey = false, None)))
                )
              )
            )
          )
        )
      }

      "all data present for partnership" in {
        val establisherDetails = EstablisherInfo(Seq(), Seq(), Seq(partnershipDetails))

        establisherInfoRows.transformSuperSection(establisherDetails) must equal(
          MasterSection(Some("messages__psaSchemeDetails__establishers"),
            Seq(SuperSection(Some("abc partnership"),
              Seq(AnswerSection(None, Seq(
                AnswerRow("messages__psaSchemeDetails__partnership_vat", Seq("789770000"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__partnership_paye", Seq("9999"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__partnership_utr", Seq("7897700000"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__partnership_address", Seq(
                  "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__partnership_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
                AnswerRow("messages__psaSchemeDetails__partnership_previous_address", Seq(
                  "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__partnership_email", Seq("test@test.com"), answerIsMessageKey = false, None),
                AnswerRow("messages__psaSchemeDetails__partnership_phone", Seq("07592113"), answerIsMessageKey = false, None)
              )))),
              SuperSection(Some("messages__psaSchemeDetails__partner_details"),
                Seq(AnswerSection(Some("fName mName lName"), Seq(
                  AnswerRow("messages__psaSchemeDetails__individual_date_of_birth", Seq("29 March 1955"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_nino", Seq("AA999999A"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_utr", Seq("1234567892"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_address", Seq(
                    "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_less_than_12months", Seq("companyAddressYears.under_a_year"), answerIsMessageKey = true, None),
                  AnswerRow("messages__psaSchemeDetails__individual_previous_address", Seq(
                    "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_email", Seq("test@test.com"), answerIsMessageKey = false, None),
                  AnswerRow("messages__psaSchemeDetails__individual_phone", Seq("07592113"), answerIsMessageKey = false, None)))))
            )
          )
        )
      }
    }
  }
}
