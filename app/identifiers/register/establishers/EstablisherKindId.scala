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

import identifiers.TypedIdentifier
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorId}
import identifiers.register.establishers.individual._
import models.person.PersonDetails
import models.register.establishers.EstablisherKind
import play.api.libs.json.{JsPath, JsResult, JsSuccess}
import utils.UserAnswers

case class EstablisherKindId(index: Int) extends TypedIdentifier[EstablisherKind] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherKindId.toString

  private def removeAllDirectors(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.getAllRecursive[PersonDetails](DirectorDetailsId.collectionPath(index)) match {
      case Some(allDirectors) if allDirectors.nonEmpty =>
        userAnswers.remove(DirectorId(index, 0)).flatMap(removeAllDirectors)
      case _ =>
        JsSuccess(userAnswers)
    }
  }

  override def cleanup(value: Option[EstablisherKind], userAnswers: UserAnswers): JsResult[UserAnswers] =
    value match {
      case Some(EstablisherKind.Indivdual) =>
        userAnswers.remove(CompanyDetailsId(index))
          .flatMap(_.remove(CompanyRegistrationNumberId(index)))
          .flatMap(_.remove(CompanyUniqueTaxReferenceId(index)))
          .flatMap(_.remove(CompanyPostCodeLookupId(index)))
          .flatMap(_.remove(CompanyAddressId(index)))
          .flatMap(_.remove(CompanyAddressYearsId(index)))
          .flatMap(_.remove(CompanyPreviousAddressPostcodeLookupId(index)))
          .flatMap(_.remove(CompanyPreviousAddressId(index)))
          .flatMap(_.remove(CompanyContactDetailsId(index)))
          .flatMap(removeAllDirectors)

      case Some(EstablisherKind.Company) =>
        userAnswers.remove(EstablisherDetailsId(index))
          .flatMap(_.remove(EstablisherNinoId(index)))
          .flatMap(_.remove(UniqueTaxReferenceId(index)))
          .flatMap(_.remove(PostCodeLookupId(index)))
          .flatMap(_.remove(AddressId(index)))
          .flatMap(_.remove(AddressYearsId(index)))
          .flatMap(_.remove(PreviousPostCodeLookupId(index)))
          .flatMap(_.remove(PreviousAddressId(index)))
          .flatMap(_.remove(ContactDetailsId(index)))

      case _ =>
        super.cleanup(value, userAnswers)
    }
}

object EstablisherKindId {
  override lazy val toString: String = "establisherKind"
}
