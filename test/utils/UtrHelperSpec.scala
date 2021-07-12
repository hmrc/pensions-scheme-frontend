/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import identifiers.register.establishers.company.CompanyEnterUTRId
import identifiers.register.establishers.company.director.DirectorEnterUTRId
import identifiers.register.establishers.partnership.PartnershipEnterUTRId
import identifiers.register.establishers.partnership.partner.PartnerEnterUTRId
import identifiers.register.trustees.individual.TrusteeUTRId
import models.ReferenceValue
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.UtrHelper.stripUtr

class UtrHelperSpec extends WordSpec with MustMatchers with OptionValues {


    "stripUtr for company" must {
      "do nothing if valid 10 digit UTR submitted" in {
        val ua = UserAnswers().setOrException(CompanyEnterUTRId(0))(ReferenceValue("1234567890"))
        val result = stripUtr(ua)

        result.get(CompanyEnterUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
      }
      "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
        val ua = UserAnswers().setOrException(CompanyEnterUTRId(0))(ReferenceValue("k1234567890123"))
        val result = stripUtr(ua)

        result.get(CompanyEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      }

      "remove any letters and first 3 digits when 13 digit UTR is submitted for second Establisher" in {
        val ua = UserAnswers()
          .setOrException(CompanyEnterUTRId(0))(ReferenceValue("k1234567890123"))
          .setOrException(CompanyEnterUTRId(1))(ReferenceValue("k1234567890321"))
        val result = stripUtr(ua)

        result.get(CompanyEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
        result.get(CompanyEnterUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
      }
    }

    "stripUtr for partnership" must {
      "do nothing if valid 10 digit UTR submitted" in {
        val ua = UserAnswers().setOrException(PartnershipEnterUTRId(0))(ReferenceValue("1234567890"))
        val result = stripUtr(ua)

        result.get(PartnershipEnterUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
      }
      "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
        val ua = UserAnswers().setOrException(PartnershipEnterUTRId(0))(ReferenceValue("k1234567890123"))
        val result = stripUtr(ua)

        result.get(PartnershipEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      }
      "remove any letters and first 3 digits when 13 digit UTR is submitted for second partnership" in {
        val ua = UserAnswers()
          .setOrException(PartnershipEnterUTRId(0))(ReferenceValue("k1234567890123"))
          .setOrException(PartnershipEnterUTRId(1))(ReferenceValue("k1234567890321"))

        val result = stripUtr(ua)

        result.get(PartnershipEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
        result.get(PartnershipEnterUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
      }
    }
  "stripUtr for directors" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers().setOrException(DirectorEnterUTRId(0, 0))(ReferenceValue("1234567890"))
      val result = stripUtr(ua)

      result.get(DirectorEnterUTRId(0, 0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers().setOrException(DirectorEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
      val result = stripUtr(ua)

      result.get(DirectorEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Director" in {
      val ua = UserAnswers()
        .setOrException(DirectorEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
        .setOrException(DirectorEnterUTRId(0,1))(ReferenceValue("k1234567890321"))
      val result = stripUtr(ua)

      result.get(DirectorEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(DirectorEnterUTRId(0, 1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }

  "stripUtr for Partner" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers().setOrException(PartnerEnterUTRId(0, 0))(ReferenceValue("1234567890"))
      val result = stripUtr(ua)

      result.get(PartnerEnterUTRId(0, 0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers().setOrException(PartnerEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
      val result = stripUtr(ua)

      result.get(PartnerEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Partner" in {
      val ua = UserAnswers()
        .setOrException(PartnerEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
        .setOrException(PartnerEnterUTRId(0,1))(ReferenceValue("k1234567890321"))
      val result = stripUtr(ua)

      result.get(PartnerEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(PartnerEnterUTRId(0, 1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }


  "stripUtr for Trustees" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers().setOrException(TrusteeUTRId(0))(ReferenceValue("1234567890"))
      val result = stripUtr(ua)

      result.get(TrusteeUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers().setOrException(TrusteeUTRId(0))(ReferenceValue("k1234567890123"))
      val result = stripUtr(ua)

      result.get(TrusteeUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Trustee" in {
      val ua = UserAnswers()
        .setOrException(TrusteeUTRId(0))(ReferenceValue("k1234567890123"))
        .setOrException(TrusteeUTRId(1))(ReferenceValue("k1234567890321"))
      val result = stripUtr(ua)

      result.get(TrusteeUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(TrusteeUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }

}
