/*
 * Copyright 2019 HM Revenue & Customs
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

package identifiers.register.trustees

import identifiers.register.trustees.company._
import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership._
import models._
import models.address.{Address, TolerantAddress}
import models.person.PersonName
import models.register.trustees.TrusteeKind
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class TrusteesKindIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import TrusteesKindIdSpec._

  "cleanup" when {

    "`TrusteeKind` changed from Company to Individual" must {
      val result = trusteeCompany.set(TrusteeKindId(0))(TrusteeKind.Individual).asOpt.value

      "remove all the data for `Company`" in {
        result mustBe UserAnswers().set(TrusteeKindId(0))(TrusteeKind.Individual).asOpt.value
      }
    }

    "`TrusteeKind` changed from Company to Partnership" must {
      val result = trusteeCompany.set(TrusteeKindId(0))(TrusteeKind.Partnership).asOpt.value

      "remove all the data for `Company`" in {
        result mustBe UserAnswers().set(TrusteeKindId(0))(TrusteeKind.Partnership).asOpt.value
      }
    }

    "`TrusteeKind` changed from Partnership to Company" must {
      val result = trusteePartnership.set(TrusteeKindId(0))(TrusteeKind.Company).asOpt.value

      "remove all the data for `Partnership`" in {
        result mustBe UserAnswers().set(TrusteeKindId(0))(TrusteeKind.Company).asOpt.value
      }
    }

    "`TrusteeKind` changed from Individual to Company" must {
      val result = trusteeIndividual.set(TrusteeKindId(0))(TrusteeKind.Company).asOpt.value

      "remove all the data for `Individual`" in {
        result mustBe UserAnswers().set(TrusteeKindId(0))(TrusteeKind.Company).asOpt.value
      }
    }
  }
}

object TrusteesKindIdSpec extends OptionValues with Enumerable.Implicits {
  // TODO 3341: Deal with email and phone id
  val trusteeCompany = UserAnswers()
    .set(TrusteeKindId(0))(TrusteeKind.Company)
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("")))
    .flatMap(_.set(HasCompanyCRNId(0))(false))
    .flatMap(_.set(HasCompanyUTRId(0))(false))
    .flatMap(_.set(CompanyPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyPreviousAddressId(0))(Address("", "", None, None, None, "")))
//    .flatMap(_.set(CompanyEmailId(0))(""))
//    .flatMap(_.set(CompanyPhoneId(0))(""))
    .asOpt.value

  val trusteeIndividual = UserAnswers(Json.obj())
    .set(TrusteeKindId(0))(TrusteeKind.Individual)
    .flatMap(_.set(TrusteeNameId(0))(PersonName("", "")))
    .flatMap(_.set(TrusteeNinoId(0))(Nino.No("")))
    .flatMap(_.set(UniqueTaxReferenceId(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(IndividualPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(TrusteeAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(TrusteeAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(TrusteePreviousAddressId(0))(Address("", "", None, None, None, "")))
//    .flatMap(_.set(TrusteeEmailId(0))(""))
//    .flatMap(_.set(TrusteePhoneId(0))(""))
    .asOpt.value

  val trusteePartnership = UserAnswers()
    .set(TrusteeKindId(0))(TrusteeKind.Partnership)
    .flatMap(_.set(PartnershipDetailsId(0))(models.PartnershipDetails("test partnership")))
    .flatMap(_.set(PartnershipPayeId(0))(Paye.No))
    .flatMap(_.set(PartnershipVatId(0))(Vat.No))
    .flatMap(_.set(PartnershipUniqueTaxReferenceId(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(PartnershipPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(partnership.PartnershipAddressListId(0))(TolerantAddress(Some(""),
      Some(""), None, None, None, None)))
    .flatMap(_.set(partnership.PartnershipAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(partnership.PartnershipAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(partnership.PartnershipPreviousAddressListId(0))(TolerantAddress(Some(""),
      Some(""), None, None, None, None)))
    .flatMap(_.set(PartnershipContactDetailsId(0))(ContactDetails("", "")))
    .asOpt.value
}
