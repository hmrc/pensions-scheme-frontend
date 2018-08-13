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

package identifiers.register.trustees

import identifiers.register.trustees.company._
import identifiers.register.trustees.individual._
import models._
import models.address.Address
import models.person.PersonDetails
import models.register.trustees.TrusteeKind
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class TrusteesKindIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import TrusteesKindIdSpec._

  "cleanup" when {

    "`TrusteeKind` changed from Company to Individual" must {
      val result = trusteeCompany.set(TrusteeKindId(0))(TrusteeKind.Individual).asOpt.value

      "remove the data for `CompanyDetails`" in {
        result.get(CompanyDetailsId(0)) mustNot be(defined)
      }
      "remove the data for `CompanyRegistrationNumber`" in {
        result.get(CompanyRegistrationNumberId(0)) mustNot be(defined)
      }
      "remove the data for `CompanyUniqueTaxReference`" in {
        result.get(CompanyUniqueTaxReferenceId(0)) mustNot be(defined)
      }
      "remove the data for `Company Address`" in {
        result.get(CompanyPostcodeLookupId(0)) mustNot be(defined)
        result.get(CompanyAddressId(0)) mustNot be(defined)
      }
      "remove the data for `CompanyAddressYears`" in {
        result.get(CompanyAddressYearsId(0)) mustNot be(defined)
      }
      "remove the data for `Previous Address`" in {
        result.get(CompanyPreviousAddressPostcodeLookupId(0)) mustNot be(defined)
        result.get(CompanyPreviousAddressId(0)) mustNot be(defined)
      }
      "remove the data for `Contact Details`" in {
        result.get(CompanyContactDetailsId(0)) mustNot be(defined)
      }
      "not remove the data for `TrusteeDetails`" in {
        result.get(TrusteeDetailsId(0)) mustBe defined
      }
    }

    "`TrusteeKind` changed from Individual to Company" must {
      val result = trusteeIndividual.set(TrusteeKindId(0))(TrusteeKind.Company).asOpt.value

      "remove the data for `TrusteeDetails`" in {
        result.get(TrusteeDetailsId(0)) mustNot be(defined)
      }
      "remove the data for `TrusteeNino`" in {
        result.get(TrusteeNinoId(0)) mustNot be(defined)
      }
      "remove the data for `UniqueTaxReference`" in {
        result.get(UniqueTaxReferenceId(0)) mustNot be(defined)
      }
      "remove the data for `Trustee Address`" in {
        result.get(IndividualPostCodeLookupId(0)) mustNot be(defined)
        result.get(TrusteeAddressId(0)) mustNot be(defined)
      }
      "remove the data for `TrusteeAddressYears`" in {
        result.get(TrusteeAddressYearsId(0)) mustNot be(defined)
      }
      "remove the data for `Trustee Previous Address`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId(0)) mustNot be(defined)
        result.get(TrusteePreviousAddressId(0)) mustNot be(defined)
      }
      "remove the data for `Trustee Contact Details`" in {
        result.get(TrusteeContactDetailsId(0)) mustNot be(defined)
      }
      "not remove the data for `Company Details`" in {
        result.get(CompanyDetailsId(0)) mustBe defined
      }
    }

    "`TrusteeKind` is removed" must {
      val result = trusteeIndividual.remove(TrusteeKindId(0)).asOpt.value

      "not remove the data for `TrusteeDetails`" in {
        result.get(TrusteeDetailsId(0)) mustBe defined
      }
      "not remove the data for `TrusteeNino`" in {
        result.get(TrusteeNinoId(0)) mustBe defined
      }
      "not remove the data for `UniqueTaxReference`" in {
        result.get(UniqueTaxReferenceId(0)) mustBe defined
      }
      "not remove the data for `Trustee Address`" in {
        result.get(IndividualPostCodeLookupId(0)) mustBe defined
        result.get(TrusteeAddressId(0)) mustBe defined
      }
      "not remove the data for `TrusteeAddressYears`" in {
        result.get(TrusteeAddressYearsId(0)) mustBe defined
      }
      "not remove the data for `Trustee Previous Address`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId(0)) mustBe defined
        result.get(TrusteePreviousAddressId(0)) mustBe defined
      }
      "not remove the data for `Trustee Contact Details`" in {
        result.get(TrusteeContactDetailsId(0)) mustBe defined
      }
      "not remove the data for `Company Details`" in {
        result.get(CompanyDetailsId(0)) mustBe defined
      }
    }
  }
}

object TrusteesKindIdSpec extends OptionValues with Enumerable.Implicits {
  val trusteeCompany = UserAnswers(Json.obj())
    .set(TrusteeKindId(0))(TrusteeKind.Company)
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("", None, None)))
    .flatMap(_.set(CompanyRegistrationNumberId(0))(CompanyRegistrationNumber.No("")))
    .flatMap(_.set(CompanyUniqueTaxReferenceId(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(CompanyPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyContactDetailsId(0))(ContactDetails("", "")))
    .flatMap(_.set(TrusteeDetailsId(0))(PersonDetails("", None, "", LocalDate.now)))
    .asOpt.value

  val trusteeIndividual = UserAnswers(Json.obj())
    .set(TrusteeKindId(0))(TrusteeKind.Individual)
    .flatMap(_.set(TrusteeDetailsId(0))(PersonDetails("", None, "", LocalDate.now)))
    .flatMap(_.set(TrusteeNinoId(0))(Nino.No("")))
    .flatMap(_.set(UniqueTaxReferenceId(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(IndividualPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(TrusteeAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(TrusteeAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(TrusteePreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(TrusteeContactDetailsId(0))(ContactDetails("", "")))
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("", None, None)))
    .asOpt.value
}
