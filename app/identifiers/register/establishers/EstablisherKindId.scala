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

import identifiers.TypedIdentifier
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorId, DirectorNameId}
import identifiers.register.establishers.individual._
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.partnership.partner.{PartnerDetailsId, PartnerId}
import models.person.{PersonDetails, PersonName}
import models.register.establishers.EstablisherKind
import play.api.libs.json.{JsPath, JsResult, JsSuccess}
import utils.UserAnswers

case class EstablisherKindId(index: Int) extends TypedIdentifier[EstablisherKind] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherKindId.toString

  override def cleanup(value: Option[EstablisherKind], userAnswers: UserAnswers): JsResult[UserAnswers] =
    value match {
      case Some(EstablisherKind.Indivdual) =>
        removeAllCompany(userAnswers)
          .flatMap(removeAllPartnership(_))

      case Some(EstablisherKind.Company) =>
        removeAllIndividual(userAnswers)
          .flatMap(removeAllPartnership(_))

      case Some(EstablisherKind.Partnership) =>
        removeAllCompany(userAnswers)
          .flatMap(removeAllIndividual(_))

      case _ =>
        super.cleanup(value, userAnswers)
    }

  private def removeAllDirectors(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.getAllRecursive[PersonName](DirectorNameId.collectionPath(index)) match {
      case Some(allDirectors) if allDirectors.nonEmpty =>
        userAnswers.remove(DirectorId(index, 0)).flatMap(removeAllDirectors)
      case _ =>
        JsSuccess(userAnswers)
    }
  }

  private def removeAllCompany(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.remove(CompanyDetailsId(index))

      .flatMap(_.remove(HasCompanyCRNId(index)))
      .flatMap(_.remove(CompanyEnterCRNId(index)))
      .flatMap(_.remove(HasCompanyUTRId(index)))
      .flatMap(_.remove(CompanyEnterUTRId(index)))

      .flatMap(_.remove(CompanyPostCodeLookupId(index)))
      .flatMap(_.remove(CompanyAddressListId(index)))
      .flatMap(_.remove(CompanyAddressId(index)))
      .flatMap(_.remove(CompanyAddressYearsId(index)))
      .flatMap(_.remove(CompanyPreviousAddressPostcodeLookupId(index)))
      .flatMap(_.remove(CompanyPreviousAddressListId(index)))
      .flatMap(_.remove(CompanyPreviousAddressId(index)))
      .flatMap(removeAllDirectors)
  }

  private def removeAllPartners(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.getAllRecursive[PersonDetails](PartnerDetailsId.collectionPath(index)) match {
      case Some(allPartners) if allPartners.nonEmpty =>
        userAnswers.remove(PartnerId(index, 0)).flatMap(removeAllPartners)
      case _ =>
        JsSuccess(userAnswers)
    }
  }

  private def removeAllPartnership(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.remove(PartnershipDetailsId(index))
      .flatMap(_.remove(PartnershipVatId(index)))
      .flatMap(_.remove(PartnershipPayeId(index)))
      .flatMap(_.remove(PartnershipUniqueTaxReferenceID(index)))
      .flatMap(_.remove(PartnershipPostcodeLookupId(index)))
      .flatMap(_.remove(PartnershipAddressListId(index)))
      .flatMap(_.remove(PartnershipAddressId(index)))
      .flatMap(_.remove(PartnershipAddressYearsId(index)))
      .flatMap(_.remove(PartnershipPreviousAddressPostcodeLookupId(index)))
      .flatMap(_.remove(PartnershipPreviousAddressListId(index)))
      .flatMap(_.remove(PartnershipPreviousAddressId(index)))
      .flatMap(_.remove(PartnershipContactDetailsId(index)))
      .flatMap(removeAllPartners)
  }

  private def removeAllIndividual(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.remove(EstablisherDetailsId(index))
      .flatMap(_.remove(EstablisherNinoId(index)))
      .flatMap(_.remove(UniqueTaxReferenceId(index)))
      .flatMap(_.remove(PostCodeLookupId(index)))
      .flatMap(_.remove(AddressListId(index)))
      .flatMap(_.remove(AddressId(index)))
      .flatMap(_.remove(AddressYearsId(index)))
      .flatMap(_.remove(PreviousPostCodeLookupId(index)))
      .flatMap(_.remove(PreviousAddressId(index)))
      .flatMap(_.remove(ContactDetailsId(index)))
  }
}

object EstablisherKindId {
  override lazy val toString: String = "establisherKind"
}
