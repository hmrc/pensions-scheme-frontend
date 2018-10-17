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

import models.details.{PartnershipDetails, _}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.FakeCountryOptions
import viewmodels.AnswerRow

import scala.language.implicitConversions

class PartnershipDetailsRowsSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with SchemeDetailsStubData {

  val partnershipDetailsRows: PartnershipDetailsRows[PartnershipDetails] = PartnershipDetailsRows[PartnershipDetails](FakeCountryOptions())

  "PartnershipDetailsRows" must {

    "produce row of answers" when {

      "all data present" in {

        partnershipDetailsRows.transformRows(partnershipDetails) must equal(Seq(
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
        ))
      }

      "vat, paye, utr and previous address are not present" in {

        partnershipDetailsRows.transformRows(partnershipDetails.copy(vatRegistration = None, payeRef= None, utr=None,
          previousAddress = PreviousAddressInfo(false, None))) must equal(Seq(
          AnswerRow("messages__psaSchemeDetails__partnership_address", Seq(
            "address line 1,", "address line 2,", "address line 3,", "AB1 1AB,", "Country of GB"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__partnership_less_than_12months", Seq("companyAddressYears.over_a_year"), answerIsMessageKey = true, None),
          AnswerRow("messages__psaSchemeDetails__partnership_email", Seq("test@test.com"), answerIsMessageKey = false, None),
          AnswerRow("messages__psaSchemeDetails__partnership_phone", Seq("07592113"), answerIsMessageKey = false, None)
        ))
      }
    }
  }
}
