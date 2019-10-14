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

package identifiers.register.establishers

import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.individual._
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import models.address.{Address, TolerantAddress}
import models.person.{PersonDetails, PersonName}
import models.register.establishers.EstablisherKind
import models.{person, _}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class EstablisherKindIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import EstablisherKindIdSpec._

  "cleanup" when {


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
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("")))
    .flatMap(_.set(HasCompanyCRNId(0))(false))
    .flatMap(_.set(CompanyEnterCRNId(0))(ReferenceValue("")))
    .flatMap(_.set(HasCompanyUTRId(0))(false))
    .flatMap(_.set(CompanyEnterUTRId(0))(ReferenceValue("")))
    .flatMap(_.set(CompanyPostCodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(CompanyPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(CompanyEmailId(0))(""))
    .flatMap(_.set(CompanyPhoneId(0))(""))
    .flatMap(_.set(DirectorNameId(0, 0))(PersonName("dir1",  "")))
    .flatMap(_.set(DirectorNameId(0, 1))(PersonName("dir2",  "")))
    .flatMap(_.set(EstablisherNameId(0))(person.PersonName("",  "")))
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
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("")))
    .asOpt.value

  val establisherPartnership = UserAnswers(Json.obj())
    .set(EstablisherKindId(0))(EstablisherKind.Partnership)
    .flatMap(_.set(PartnershipDetailsId(0))(PartnershipDetails("")))
    .flatMap(_.set(PartnershipVatId(0))(Vat.No))
    .flatMap(_.set(PartnershipPayeId(0))(Paye.No))
    .flatMap(_.set(PartnershipUniqueTaxReferenceID(0))(UniqueTaxReference.No("")))
    .flatMap(_.set(PartnershipPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(PartnershipAddressListId(0))(TolerantAddress(Some(""), Some(""), None, None, None, Some(""))))
    .flatMap(_.set(PartnershipAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnershipPreviousAddressPostcodeLookupId(0))(Seq.empty))
    .flatMap(_.set(PartnershipPreviousAddressListId(0))(TolerantAddress(Some(""), Some(""), None, None, None, Some(""))))
    .flatMap(_.set(PartnershipPreviousAddressId(0))(Address("", "", None, None, None, "")))
    .flatMap(_.set(PartnershipContactDetailsId(0))(ContactDetails("", "")))
    .flatMap(_.set(PartnerDetailsId(0, 0))(PersonDetails("par1", None, "", LocalDate.now)))
    .flatMap(_.set(PartnerDetailsId(0, 1))(PersonDetails("par2", None, "", LocalDate.now)))
    .asOpt.value
}
