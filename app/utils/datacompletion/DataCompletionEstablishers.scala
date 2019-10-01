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
  def isEstablisherCompanyDetailsComplete(index: Int, mode: Mode): Option[Boolean] = {
    isComplete(
      Seq(
        isAnswerComplete(HasCompanyCRNId(index), CompanyEnterCRNId(index), Some(CompanyNoCRNReasonId(index))),
        isAnswerComplete(HasCompanyUTRId(index), CompanyEnterUTRId(index), Some(CompanyNoUTRReasonId(index))),
        isAnswerComplete(HasCompanyVATId(index), CompanyEnterVATId(index), None),
        isAnswerComplete(HasCompanyPAYEId(index), CompanyEnterPAYEId(index), None)
      ) ++ (if (mode == NormalMode) Seq(isAnswerComplete(IsCompanyDormantId(index))) else Nil)
    )
  }

  def isEstablisherCompanyAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(CompanyAddressId(index), CompanyPreviousAddressId(index), CompanyAddressYearsId(index), Some(HasBeenTradingCompanyId(index)))

  def isEstablisherCompanyContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(CompanyEmailId(index), CompanyPhoneId(index))

  def isEstablisherCompanyComplete(index: Int, mode: Mode): Boolean = {
    isComplete(
      Seq(isEstablisherCompanyDetailsComplete(index, mode), isEstablisherCompanyAddressComplete(index),
        isEstablisherCompanyContactDetailsComplete(index)))
      .getOrElse(false)
  }

  def isEstablisherCompanyAndDirectorsComplete(index: Int, mode: Mode): Boolean = {
    val allDirectors = allDirectorsAfterDelete(index)
    val allDirectorsCompleted = allDirectors.nonEmpty & allDirectors.forall(_.isCompleted)
    val isCompanyComplete = isEstablisherCompanyComplete(index, mode)
    allDirectorsCompleted & isCompanyComplete
  }

  //DIRECTORS

  def isDirectorDetailsComplete(estIndex: Int, dirIndex: Int): Option[Boolean] =
    isComplete(Seq(
      Some(get(DirectorDOBId(estIndex, dirIndex)).isDefined),
      isAnswerComplete(DirectorHasNINOId(estIndex, dirIndex), DirectorEnterNINOId(estIndex, dirIndex), Some(DirectorNoNINOReasonId(estIndex, dirIndex))),
      isAnswerComplete(DirectorHasUTRId(estIndex, dirIndex), DirectorEnterUTRId(estIndex, dirIndex), Some(DirectorNoUTRReasonId(estIndex, dirIndex)))
    ))

  def isDirectorCompleteHnS(estIndex: Int, dirIndex: Int): Boolean =
    isComplete(Seq(
      isDirectorDetailsComplete(estIndex, dirIndex),
      isAddressComplete(DirectorAddressId(estIndex, dirIndex), DirectorPreviousAddressId(estIndex, dirIndex),
        DirectorAddressYearsId(estIndex, dirIndex), None),
      isContactDetailsComplete(DirectorEmailId(estIndex, dirIndex), DirectorPhoneNumberId(estIndex, dirIndex)))).getOrElse(false)

  //ESTABLISHER INDIVIDUAL

  def isEstablisherIndividualDetailsComplete(establisherIndex: Int): Option[Boolean] =
    isComplete(Seq(
      isAnswerComplete(EstablisherDOBId(establisherIndex)),
      isAnswerComplete(EstablisherHasNINOId(establisherIndex), EstablisherEnterNINOId(establisherIndex), Some(EstablisherNoNINOReasonId(establisherIndex))),
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
        get(EstablisherNinoId(index)).isDefined | get(EstablisherEnterNINOId(index)).isDefined,
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
        isAnswerComplete(PartnershipHasUTRId(index), PartnershipEnterUTRId(index), Some(PartnershipNoUTRReasonId(index))),
        isAnswerComplete(PartnershipHasVATId(index), PartnershipEnterVATId(index), None),
        isAnswerComplete(PartnershipHasPAYEId(index), PartnershipEnterPAYEId(index), None)
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
      get(PartnershipUniqueTaxReferenceID(index)).isDefined | get(PartnershipEnterUTRId(index)).isDefined,
      get(PartnershipVatId(index)).isDefined | get(PartnershipEnterVATId(index)).isDefined,
      get(PartnershipPayeId(index)).isDefined | get(PartnershipEnterPAYEId(index)).isDefined,
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
