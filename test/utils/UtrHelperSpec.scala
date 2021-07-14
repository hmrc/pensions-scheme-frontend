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

import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.CompanyEnterUTRId
import identifiers.register.establishers.company.director.{DirectorEnterUTRId, DirectorNameId}
import identifiers.register.establishers.partnership.PartnershipEnterUTRId
import identifiers.register.establishers.partnership.partner.{PartnerEnterUTRId, PartnerNameId}
import identifiers.register.trustees.TrusteeKindId
import identifiers.register.trustees.company.{CompanyEnterUTRId => TrusteeCompanyUTRId}
import identifiers.register.trustees.individual.TrusteeUTRId
import models.ReferenceValue
import models.person.PersonName
import models.register.establishers.EstablisherKind._
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.Individual
import org.scalatest.{MustMatchers, OptionValues, WordSpec}

class UtrHelperSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "countEstablishers" must {
    "return correct number of establishers" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Company)
        .setOrException(EstablisherKindId(1))(Indivdual)
        .setOrException(EstablisherKindId(2))(Partnership)

      UtrHelper.countEstablishers(ua) mustBe 3
    }
  }

  "countDirectors" must {
    "return correct number of Directors" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Company)
        .setOrException(DirectorNameId(0, 0))(PersonName("", ""))
        .setOrException(DirectorNameId(0, 1))(PersonName("", ""))
        .setOrException(DirectorNameId(0, 2))(PersonName("", ""))
      UtrHelper.countDirectors(ua, 0) mustBe 3
    }
  }

  "countPartners" must {
    "return correct number of Partners" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Partnership)
        .setOrException(PartnerNameId(0, 0))(PersonName("", ""))
        .setOrException(PartnerNameId(0, 1))(PersonName("", ""))
        .setOrException(PartnerNameId(0, 2))(PersonName("", ""))
      UtrHelper.countPartners(ua, 0) mustBe 3
    }
  }

    "stripUtr for company" must {
      "do nothing if valid 10 digit UTR submitted" in {
        val ua = UserAnswers()
          .setOrException(EstablisherKindId(0))(Company)
          .setOrException(CompanyEnterUTRId(0))(ReferenceValue("1234567890"))
        val result = UtrHelper.stripUtr(ua)

        result.get(CompanyEnterUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
      }
      "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
        val ua = UserAnswers()
          .setOrException(EstablisherKindId(0))(Company)
          .setOrException(CompanyEnterUTRId(0))(ReferenceValue("k1234567890123"))
        val result = UtrHelper.stripUtr(ua)

        result.get(CompanyEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      }

      "remove any letters and first 3 digits when 13 digit UTR is submitted for second Establisher" in {
        val ua = UserAnswers()
          .setOrException(EstablisherKindId(0))(Company)
          .setOrException(CompanyEnterUTRId(0))(ReferenceValue("k1234567890123"))
          .setOrException(EstablisherKindId(1))(Company)
          .setOrException(CompanyEnterUTRId(1))(ReferenceValue("k1234567890321"))
        val result = UtrHelper.stripUtr(ua)

        result.get(CompanyEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
        result.get(CompanyEnterUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
      }
    }

    "stripUtr for partnership" must {
      "do nothing if valid 10 digit UTR submitted" in {
        val ua = UserAnswers()
          .setOrException(EstablisherKindId(0))(Partnership)
          .setOrException(PartnershipEnterUTRId(0))(ReferenceValue("1234567890"))
        val result = UtrHelper.stripUtr(ua)

        result.get(PartnershipEnterUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
      }
      "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
        val ua = UserAnswers()
          .setOrException(EstablisherKindId(0))(Partnership)
          .setOrException(PartnershipEnterUTRId(0))(ReferenceValue("k1234567890123"))
        val result = UtrHelper.stripUtr(ua)

        result.get(PartnershipEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      }
      "remove any letters and first 3 digits when 13 digit UTR is submitted for second partnership" in {
        val ua = UserAnswers()
          .setOrException(EstablisherKindId(0))(Partnership)
          .setOrException(PartnershipEnterUTRId(0))(ReferenceValue("k1234567890123"))
          .setOrException(EstablisherKindId(1))(Partnership)
          .setOrException(PartnershipEnterUTRId(1))(ReferenceValue("k1234567890321"))

        val result = UtrHelper.stripUtr(ua)

        result.get(PartnershipEnterUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
        result.get(PartnershipEnterUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
      }
    }

  "stripUtr for Trustee Individual" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers()
        .setOrException(TrusteeKindId(0))(Individual)
        .setOrException(TrusteeUTRId(0))(ReferenceValue("1234567890"))
      val result = UtrHelper.stripUtr(ua)

      result.get(TrusteeUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers()
        .setOrException(TrusteeKindId(0))(Individual)
        .setOrException(TrusteeUTRId(0))(ReferenceValue("k1234567890123"))
      val result = UtrHelper.stripUtr(ua)

      result.get(TrusteeUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
    }

    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Trustee Individual" in {
      val ua = UserAnswers()
        .setOrException(TrusteeKindId(0))(Individual)
        .setOrException(TrusteeUTRId(0))(ReferenceValue("k1234567890123"))
        .setOrException(TrusteeKindId(1))(Individual)
        .setOrException(TrusteeUTRId(1))(ReferenceValue("k1234567890321"))
      val result = UtrHelper.stripUtr(ua)

      result.get(TrusteeUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(TrusteeUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }

  "stripUtr for directors" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Company)
        .setOrException(DirectorNameId(0, 0))(PersonName("", ""))
        .setOrException(DirectorEnterUTRId(0, 0))(ReferenceValue("1234567890"))
      val result = UtrHelper.stripUtr(ua)

      result.get(DirectorEnterUTRId(0, 0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Company)
        .setOrException(DirectorNameId(0, 0))(PersonName("", ""))
        .setOrException(DirectorEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
      val result = UtrHelper.stripUtr(ua)

      result.get(DirectorEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Director" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Company)
        .setOrException(DirectorNameId(0, 0))(PersonName("", ""))
        .setOrException(DirectorEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
        .setOrException(DirectorNameId(0, 1))(PersonName("", ""))
        .setOrException(DirectorEnterUTRId(0,1))(ReferenceValue("k1234567890321"))
      val result = UtrHelper.stripUtr(ua)

      result.get(DirectorEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(DirectorEnterUTRId(0, 1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }

  "stripUtr for Partner" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Partnership)
        .setOrException(PartnerNameId(0, 0))(PersonName("", ""))
        .setOrException(PartnerEnterUTRId(0, 0))(ReferenceValue("1234567890"))
      val result = UtrHelper.stripUtr(ua)

      result.get(PartnerEnterUTRId(0, 0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Partnership)
        .setOrException(PartnerNameId(0, 0))(PersonName("", ""))
        .setOrException(PartnerEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
      val result = UtrHelper.stripUtr(ua)

      result.get(PartnerEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Partner" in {
      val ua = UserAnswers()
        .setOrException(EstablisherKindId(0))(Partnership)
        .setOrException(PartnerNameId(0, 0))(PersonName("", ""))
        .setOrException(PartnerEnterUTRId(0, 0))(ReferenceValue("k1234567890123"))
        .setOrException(PartnerNameId(0, 1))(PersonName("", ""))
        .setOrException(PartnerEnterUTRId(0,1))(ReferenceValue("k1234567890321"))
      val result = UtrHelper.stripUtr(ua)

      result.get(PartnerEnterUTRId(0, 0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(PartnerEnterUTRId(0, 1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }


  "stripUtr for Trustees" must {
    "do nothing if valid 10 digit UTR submitted" in {
      val ua = UserAnswers()
        .setOrException(TrusteeKindId(0))(TrusteeKind.Company)
        .setOrException(TrusteeCompanyUTRId(0))(ReferenceValue("1234567890"))
      val result = UtrHelper.stripUtr(ua)

      result.get(TrusteeCompanyUTRId(0)) mustBe Some(ReferenceValue("1234567890"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted" in {
      val ua = UserAnswers()
        .setOrException(TrusteeKindId(0))(TrusteeKind.Company)
        .setOrException(TrusteeCompanyUTRId(0))(ReferenceValue("k1234567890123"))
      val result = UtrHelper.stripUtr(ua)

      result.get(TrusteeCompanyUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
    }
    "remove any letters and first 3 digits when 13 digit UTR is submitted for second Trustee" in {
      val ua = UserAnswers()
        .setOrException(TrusteeKindId(0))(TrusteeKind.Company)
        .setOrException(TrusteeCompanyUTRId(0))(ReferenceValue("k1234567890123"))
        .setOrException(TrusteeKindId(1))(TrusteeKind.Company)
        .setOrException(TrusteeCompanyUTRId(1))(ReferenceValue("k1234567890321"))
      val result = UtrHelper.stripUtr(ua)

      result.get(TrusteeCompanyUTRId(0)) mustBe Some(ReferenceValue("4567890123"))
      result.get(TrusteeCompanyUTRId(1)) mustBe Some(ReferenceValue("4567890321"))
    }
  }

}
