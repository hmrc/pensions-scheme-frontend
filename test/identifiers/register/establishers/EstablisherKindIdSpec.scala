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

package identifiers.register.establishers

import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.individual._
import models._
import models.address.Address
import models.person.PersonDetails
import models.register.establishers.EstablisherKind
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class EstablisherKindIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import EstablisherKindIdSpec._

  "cleanup" when {

    "`EstablisherKind` changed from Company to Individual" must {
      val result = establisherCompany.set(EstablisherKindId(0))(EstablisherKind.Indivdual).asOpt.value

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
        result.get(CompanyPostCodeLookupId(0)) mustNot be(defined)
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
      "remove the data for `Directors`" in {
        result.get(DirectorDetailsId(0, 0)) mustNot be(defined)
        result.get(DirectorDetailsId(0, 1)) mustNot be(defined)
      }
      "not remove the data for `EstablisherDetails`" in {
        result.get(EstablisherDetailsId(0)) mustBe defined
      }
    }

    "`EstablisherKind` changed from Individual to Company" must {
      val result = establisherIndividual.set(EstablisherKindId(0))(EstablisherKind.Company).asOpt.value

      "remove the data for `EstablisherDetails`" in {
        result.get(EstablisherDetailsId(0)) mustNot be(defined)
      }
      "remove the data for `EstablisherNino`" in {
        result.get(EstablisherNinoId(0)) mustNot be(defined)
      }
      "remove the data for `UniqueTaxReference`" in {
        result.get(UniqueTaxReferenceId(0)) mustNot be(defined)
      }
      "remove the data for `Individual Address`" in {
        result.get(PostCodeLookupId(0)) mustNot be(defined)
        result.get(AddressId(0)) mustNot be(defined)
      }
      "remove the data for `AddressYears`" in {
        result.get(AddressYearsId(0)) mustNot be(defined)
      }
      "remove the data for `Previous Address`" in {
        result.get(PreviousPostCodeLookupId(0)) mustNot be(defined)
        result.get(PreviousAddressId(0)) mustNot be(defined)
      }
      "remove the data for `Contact Details`" in {
        result.get(ContactDetailsId(0)) mustNot be(defined)
      }
      "not remove the data for `Company Details`" in {
        result.get(CompanyDetailsId(0)) mustBe defined
      }
    }

    "`EstablisherKind` is removed" must {
      val result = establisherIndividual.remove(EstablisherKindId(0)).asOpt.value

      "not remove the data for `Establisher Details`" in {
        result.get(EstablisherDetailsId(0)) mustBe defined
      }
      "not remove the data for `EstablisherNino`" in {
        result.get(EstablisherNinoId(0)) mustBe defined
      }
      "not remove the data for `UniqueTaxReference`" in {
        result.get(UniqueTaxReferenceId(0)) mustBe defined
      }
      "not remove the data for `Individual Address`" in {
        result.get(PostCodeLookupId(0)) mustBe defined
        result.get(AddressId(0)) mustBe defined
      }
      "not remove the data for `AddressYears`" in {
        result.get(AddressYearsId(0)) mustBe defined
      }
      "not remove the data for `Previous Address`" in {
        result.get(PreviousPostCodeLookupId(0)) mustBe defined
        result.get(PreviousAddressId(0)) mustBe defined
      }
      "not remove the data for `Contact Details`" in {
        result.get(ContactDetailsId(0)) mustBe defined
      }
      "not remove the data for `Company Details`" in {
        result.get(CompanyDetailsId(0)) mustBe defined
      }
    }
  }
}

object EstablisherKindIdSpec extends OptionValues with Enumerable.Implicits {
  val establisherCompany = UserAnswers(Json.obj())
    .set(EstablisherKindId(0))(EstablisherKind.Company)
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("", None, None)))
    .flatMap(_.set(CompanyRegistrationNumberId(0))(CompanyRegistrationNumber.No("")))
    .flatMap(_.set(CompanyUniqueTaxReferenceId(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(CompanyPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyContactDetailsId(0))(ContactDetails("", "")))
    .flatMap(_.set(EstablisherDetailsId(0))(PersonDetails("", None, "", LocalDate.now)))
    .flatMap(_.set(DirectorDetailsId(0, 0))(DirectorDetails("dir1", None, "", LocalDate.now)))
    .flatMap(_.set(DirectorDetailsId(0, 1))(DirectorDetails("dir2", None, "", LocalDate.now)))
    .asOpt.value

  val establisherIndividual = UserAnswers(Json.obj())
    .set(EstablisherKindId(0))(EstablisherKind.Indivdual)
    .flatMap(_.set(EstablisherDetailsId(0))(PersonDetails("", None, "", LocalDate.now)))
    .flatMap(_.set(EstablisherNinoId(0))(Nino.No("")))
    .flatMap(_.set(UniqueTaxReferenceId(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(PostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(AddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(AddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(PreviousPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(PreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(ContactDetailsId(0))(ContactDetails("", "")))
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("", None, None)))
    .asOpt.value
}
