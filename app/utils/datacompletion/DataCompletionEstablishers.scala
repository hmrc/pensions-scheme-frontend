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

package utils.datacompletion

import identifiers.register.establishers.company.director._
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.company._
import identifiers.register.establishers.individual._
import models.{Mode, NormalMode}
import utils.UserAnswers

trait DataCompletionEstablishers extends DataCompletion {

  self: UserAnswers =>

  //ESTABLISHER COMPANY
  def isEstablisherCompanyDetailsComplete(index: Int, mode: Mode): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(HasCompanyNumberId(index), CompanyRegistrationNumberVariationsId(index), Some(NoCompanyNumberId(index))),
        isAnswerComplete(HasCompanyUTRId(index), CompanyUTRId(index), Some(NoCompanyUTRId(index))),
        isAnswerComplete(HasCompanyVATId(index), CompanyEnterVATId(index), None),
        isAnswerComplete(HasCompanyPAYEId(index), CompanyPayeVariationsId(index), None)
      ) ++ (if (mode == NormalMode) Seq(isAnswerComplete(IsCompanyDormantId(index))) else Nil)
    )

  def isEstablisherCompanyAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(CompanyAddressId(index), CompanyPreviousAddressId(index), CompanyAddressYearsId(index), Some(HasBeenTradingCompanyId(index)))

  def isEstablisherCompanyContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(CompanyEmailId(index), CompanyPhoneId(index))

  def isEstablisherCompanyCompleteNonHns(index: Int, mode: Mode): Boolean =
    isListComplete(Seq(
      get(CompanyDetailsId(index)).isDefined,
      get(CompanyRegistrationNumberId(index)).isDefined | get(CompanyRegistrationNumberVariationsId(index)).isDefined,
      get(CompanyUniqueTaxReferenceId(index)).isDefined | get(CompanyUTRId(index)).isDefined,
      get(CompanyVatId(index)).isDefined | get(CompanyEnterVATId(index)).isDefined,
      get(CompanyPayeId(index)).isDefined | get(CompanyPayeVariationsId(index)).isDefined,
      if(mode==NormalMode) get(IsCompanyDormantId(index)).isDefined else true,
      isAddressComplete(CompanyAddressId(index), CompanyPreviousAddressId(index), CompanyAddressYearsId(index), None).getOrElse(false),
      get(CompanyContactDetailsId(index)).isDefined
    ))

  def isEstablisherCompanyComplete(index: Int, mode: Mode, isHnSEnabled: Boolean): Boolean =
    if (isHnSEnabled)
      isComplete(Seq(
        isEstablisherCompanyDetailsComplete(index, mode),
        isEstablisherCompanyAddressComplete(index),
        isEstablisherCompanyContactDetailsComplete(index))).getOrElse(false)
    else
      isEstablisherCompanyCompleteNonHns(index, mode)

  def isEstablisherCompanyAndDirectorsComplete(index: Int, mode: Mode, isHnSEnabled: Boolean): Boolean = {
    val allDirectors = allDirectorsAfterDelete(index, isHnSEnabled)
    val allDirectorsCompleted = allDirectors.nonEmpty & allDirectors.forall(_.isCompleted)
    val isCompanyComplete = isEstablisherCompanyComplete(index, mode, isHnSEnabled)
    allDirectorsCompleted & isCompanyComplete
  }

  //DIRECTORS

  def isDirectorDetailsComplete(estIndex: Int, dirIndex: Int): Option[Boolean] =
    isComplete(Seq(
      Some(get(DirectorDOBId(estIndex, dirIndex)).isDefined),
      isAnswerComplete(DirectorHasNINOId(estIndex, dirIndex), DirectorNewNinoId(estIndex, dirIndex), Some(DirectorNoNINOReasonId(estIndex, dirIndex))),
      isAnswerComplete(DirectorHasUTRId(estIndex, dirIndex), DirectorUTRId(estIndex, dirIndex), Some(DirectorNoUTRReasonId(estIndex, dirIndex)))
    ))

  def isDirectorCompleteHnS(estIndex: Int, dirIndex: Int): Boolean =
    isComplete(Seq(
      isDirectorDetailsComplete(estIndex, dirIndex),
      isAddressComplete(DirectorAddressId(estIndex, dirIndex), DirectorPreviousAddressId(estIndex, dirIndex),
        DirectorAddressYearsId(estIndex, dirIndex), None),
      isContactDetailsComplete(DirectorEmailId(estIndex, dirIndex), DirectorPhoneNumberId(estIndex, dirIndex)))).getOrElse(false)

  def isDirectorCompleteNonHnS(estIndex: Int, dirIndex: Int): Boolean =
    isListComplete(Seq(
      get(DirectorDetailsId(estIndex, dirIndex)).isDefined,
      get(DirectorNinoId(estIndex, dirIndex)).isDefined | get(DirectorNewNinoId(estIndex, dirIndex)).isDefined,
      get(DirectorUniqueTaxReferenceId(estIndex, dirIndex)).isDefined | get(DirectorUTRId(estIndex, dirIndex)).isDefined,
      isAddressComplete(DirectorAddressId(estIndex, dirIndex), DirectorPreviousAddressId(estIndex, dirIndex),
        DirectorAddressYearsId(estIndex, dirIndex), None).getOrElse(false),
      get(DirectorContactDetailsId(estIndex, dirIndex)).isDefined
    ))

  //ESTABLISHER INDIVIDUAL

  def isEstablisherIndividualDetailsComplete(establisherIndex: Int): Option[Boolean] =
    isComplete(Seq(
      isAnswerComplete(EstablisherDOBId(establisherIndex)),
      isAnswerComplete(EstablisherHasNINOId(establisherIndex), EstablisherNewNinoId(establisherIndex), Some(EstablisherNoNINOReasonId(establisherIndex))),
      isAnswerComplete(EstablisherHasUTRId(establisherIndex), EstablisherUTRId(establisherIndex), Some(EstablisherNoUTRReasonId(establisherIndex)))
    ))

  def isEstablisherIndividualAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(AddressId(index), PreviousAddressId(index), AddressYearsId(index), None)

  def isEstablisherIndividualContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(EstablisherEmailId(index), EstablisherPhoneId(index))

  def isEstablisherIndividualComplete(isHnSEnabled: Boolean, index: Int): Boolean =
    if (isHnSEnabled) {
      isComplete(
        Seq(
          isEstablisherIndividualDetailsComplete(index),
          isEstablisherIndividualAddressComplete(index),
          isEstablisherIndividualContactDetailsComplete(index)
        )
      ).getOrElse(false)
    } else {
      isListComplete(Seq(
        get(EstablisherDetailsId(index)).isDefined,
        get(EstablisherNinoId(index)).isDefined | get(EstablisherNewNinoId(index)).isDefined,
        get(UniqueTaxReferenceId(index)).isDefined | get(EstablisherUTRId(index)).isDefined,
        isAddressComplete(AddressId(index), PreviousAddressId(index),
          AddressYearsId(index), None).getOrElse(false),
        get(ContactDetailsId(index)).isDefined
      ))
    }


  //ESTABLISHER PARTNERSHIP
  def isEstablisherPartnershipDetailsComplete(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(PartnershipHasUTRId(index), PartnershipUTRId(index), Some(PartnershipNoUTRReasonId(index))),
        isAnswerComplete(PartnershipHasVatId(index), PartnershipEnterVATId(index), None),
        isAnswerComplete(PartnershipHasPAYEId(index), PartnershipPayeVariationsId(index), None)
      )
    )

  def isEstablisherPartnershipAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(PartnershipAddressId(index), PartnershipPreviousAddressId(index),
      PartnershipAddressYearsId(index), Some(PartnershipHasBeenTradingId(index)))

  def isEstablisherPartnershipContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(PartnershipEmailId(index), PartnershipPhoneNumberId(index))

  def isEstablisherPartnershipCompleteNonHns(index: Int, mode: Mode): Boolean =
    isListComplete(Seq(
      get(PartnershipDetailsId(index)).isDefined,
      get(PartnershipUniqueTaxReferenceID(index)).isDefined | get(PartnershipUTRId(index)).isDefined,
      get(PartnershipVatId(index)).isDefined | get(PartnershipEnterVATId(index)).isDefined,
      get(PartnershipPayeId(index)).isDefined | get(PartnershipPayeVariationsId(index)).isDefined,
      isAddressComplete(PartnershipAddressId(index), PartnershipPreviousAddressId(index), PartnershipAddressYearsId(index), None).getOrElse(false),
      get(PartnershipContactDetailsId(index)).isDefined
    ))

  def isEstablisherPartnershipComplete(index: Int, mode: Mode, isHnSEnabled: Boolean): Boolean =
    if (isHnSEnabled)
      isComplete(Seq(
        isEstablisherPartnershipDetailsComplete(index),
        isEstablisherPartnershipAddressComplete(index),
        isEstablisherPartnershipContactDetailsComplete(index))).getOrElse(false)
    else
      isEstablisherPartnershipCompleteNonHns(index, mode)
}
