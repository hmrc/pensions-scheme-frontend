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

package utils

import identifiers.TypedIdentifier
import identifiers.register.establishers.company._
import identifiers.register.trustees.{company => tc}
import identifiers.register.trustees.{partnership => tp}
import identifiers.register.establishers.company.director._
import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership.PartnershipHasBeenTradingId
import models.address.Address
import models.{AddressYears, Mode, NormalMode, ReferenceValue}
import play.api.libs.json.Reads

trait DataCompletion {


  self: UserAnswers =>

  //GENERIC METHODS
  def isComplete(list: Seq[Option[Boolean]]): Option[Boolean] =
    if(list.flatten.isEmpty) None
    else
      Some(list.foldLeft(true)({
        case (acc , Some(true)) => acc
        case (_, Some(false)) => false
        case (_, None) => false
      }))

  def isListComplete(list: Seq[Boolean]): Boolean =
    list.nonEmpty & list.foldLeft(true)({
      case (acc , true) => acc
      case (_, false) => false
    })

  def isAddressComplete(currentAddressId: TypedIdentifier[Address],
                        previousAddressId: TypedIdentifier[Address],
                        timeAtAddress: TypedIdentifier[AddressYears],
                        tradingTime: Option[TypedIdentifier[Boolean]]
                       ): Option[Boolean] =
    (get(currentAddressId), get(timeAtAddress)) match {
      case (Some(_), Some(AddressYears.OverAYear)) => Some(true)
      case (None, _) => None
      case (Some(_), Some(AddressYears.UnderAYear)) =>
        (get(previousAddressId), tradingTime) match {
          case (Some(_), _) => Some(true)
          case (_, Some(tradingTimeId)) => Some(!get(tradingTimeId).getOrElse(true))
          case _ => Some(false)
        }
      case _ => Some(false)
    }

  def isContactDetailsComplete(emailId: TypedIdentifier[String],
                               phoneId: TypedIdentifier[String]): Option[Boolean] =
    (get(emailId), get(phoneId)) match {
      case (Some(_), Some(_)) => Some(true)
      case (None, None) => None
      case _ => Some(false)
    }

  def isAnswerComplete(yesNoQuestionId: TypedIdentifier[Boolean],
                              yesValueId: TypedIdentifier[ReferenceValue],
                              noReasonIdOpt: Option[TypedIdentifier[String]]): Option[Boolean] =
    (get(yesNoQuestionId), get(yesValueId), noReasonIdOpt) match {
      case (None, None, _) => None
      case (_, Some(_), _) => Some(true)
      case (_, _, Some(noReasonId)) if get(noReasonId).isDefined => Some(true)
      case (Some(false), _, None) => Some(true)
      case _ => Some(false)
    }

  def isAnswerComplete[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[Boolean] =
    get(id) match {
      case None => None
      case Some(_) => Some(true)
    }

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

  //TRUSTEE COMPANY
  def isTrusteeCompanyDetailsComplete(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(tc.HasCompanyNumberId(index), tc.CompanyRegistrationNumberVariationsId(index), Some(tc.NoCompanyNumberId(index))),
        isAnswerComplete(tc.HasCompanyUTRId(index), tc.CompanyUTRId(index), Some(tc.CompanyNoUTRReasonId(index))),
        isAnswerComplete(tc.HasCompanyVATId(index), tc.CompanyEnterVATId(index), None),
        isAnswerComplete(tc.HasCompanyPAYEId(index), tc.CompanyPayeVariationsId(index), None)
      )
    )

  def isTrusteeCompanyAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(tc.CompanyAddressId(index), tc.CompanyPreviousAddressId(index), tc.CompanyAddressYearsId(index), Some(tc.HasBeenTradingCompanyId(index)))

  def isTrusteeCompanyContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(tc.CompanyEmailId(index), tc.CompanyPhoneId(index))

  def isTrusteeCompanyCompleteNonHns(index: Int): Boolean =
    isListComplete(Seq(
      get(tc.CompanyDetailsId(index)).isDefined,
      get(tc.CompanyRegistrationNumberId(index)).isDefined | get(tc.CompanyRegistrationNumberVariationsId(index)).isDefined,
      get(tc.CompanyUniqueTaxReferenceId(index)).isDefined | get(tc.CompanyUTRId(index)).isDefined,
      get(tc.CompanyVatId(index)).isDefined | get(tc.CompanyEnterVATId(index)).isDefined,
      get(tc.CompanyPayeId(index)).isDefined | get(tc.CompanyPayeVariationsId(index)).isDefined,
      isAddressComplete(tc.CompanyAddressId(index), tc.CompanyPreviousAddressId(index), tc.CompanyAddressYearsId(index), None).getOrElse(false),
      get(tc.CompanyContactDetailsId(index)).isDefined
    ))

  def isTrusteeCompanyComplete(index: Int, isHnSEnabled: Boolean): Boolean =
    if (isHnSEnabled)
      isComplete(Seq(
        isTrusteeCompanyDetailsComplete(index),
        isTrusteeCompanyAddressComplete(index),
        isTrusteeCompanyContactDetailsComplete(index))).getOrElse(false)
    else
      isTrusteeCompanyCompleteNonHns(index)

  //TRUSTEE INDIVIDUAL

  def isTrusteeIndividualDetailsComplete(trusteeIndex: Int): Option[Boolean] = {
    isComplete(Seq(
      isAnswerComplete(TrusteeDOBId(trusteeIndex)),
      isAnswerComplete(TrusteeHasNINOId(trusteeIndex), TrusteeNewNinoId(trusteeIndex), Some(TrusteeNoNINOReasonId(trusteeIndex))),
      isAnswerComplete(TrusteeHasUTRId(trusteeIndex), TrusteeUTRId(trusteeIndex), Some(TrusteeNoUTRReasonId(trusteeIndex)))
    ))
  }

  def isTrusteeIndividualAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(TrusteeAddressId(index), TrusteePreviousAddressId(index), TrusteeAddressYearsId(index), None)

  def isTrusteeIndividualContactDetailsComplete(index: Int): Option[Boolean] = isContactDetailsComplete(TrusteeEmailId(index), TrusteePhoneId(index))

  def isTrusteeIndividualComplete(isHnSEnabled: Boolean, index: Int): Boolean =
    if (isHnSEnabled) {
      isComplete(
        Seq(
          isTrusteeIndividualDetailsComplete(index),
          isTrusteeIndividualAddressComplete(index),
          isTrusteeIndividualContactDetailsComplete(index)
        )
      ).getOrElse(false)
    } else {
      isListComplete(Seq(
        get(TrusteeDetailsId(index)).isDefined,
        get(TrusteeNinoId(index)).isDefined | get(TrusteeNewNinoId(index)).isDefined,
        get(UniqueTaxReferenceId(index)).isDefined | get(TrusteeUTRId(index)).isDefined,
        isAddressComplete(TrusteeAddressId(index), TrusteePreviousAddressId(index),
          TrusteeAddressYearsId(index), None).getOrElse(false),
        get(TrusteeContactDetailsId(index)).isDefined
      ))
    }

  //TRUSTEE PARTNERSHIP
  def isTrusteePartnershipDetailsComplete(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(tp.PartnershipHasUTRId(index), tp.PartnershipUTRId(index), Some(tp.PartnershipNoUTRReasonId(index))),
        isAnswerComplete(tp.PartnershipHasVATId(index), tp.PartnershipEnterVATId(index), None),
        isAnswerComplete(tp.PartnershipHasPAYEId(index), tp.PartnershipPayeVariationsId(index), None)
      )
    )

  def isTrusteePartnershipAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(tp.PartnershipAddressId(index), tp.PartnershipPreviousAddressId(index), tp.PartnershipAddressYearsId(index),
      Some(PartnershipHasBeenTradingId(index)))

  def isTrusteePartnershipContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(tp.PartnershipEmailId(index), tp.PartnershipPhoneId(index))

  def isTrusteePartnershipCompleteNonHns(index: Int): Boolean =
    isListComplete(Seq(
      get(tp.PartnershipDetailsId(index)).isDefined,
      get(tp.PartnershipUniqueTaxReferenceId(index)).isDefined | get(tp.PartnershipUTRId(index)).isDefined,
      get(tp.PartnershipVatId(index)).isDefined | get(tp.PartnershipEnterVATId(index)).isDefined,
      get(tp.PartnershipPayeId(index)).isDefined | get(tp.PartnershipPayeVariationsId(index)).isDefined,
      isAddressComplete(tp.PartnershipAddressId(index), tp.PartnershipPreviousAddressId(index), tp.PartnershipAddressYearsId(index), None).getOrElse(false),
      get(tp.PartnershipContactDetailsId(index)).isDefined
    ))

  def isTrusteePartnershipComplete(index: Int, isHnSEnabled: Boolean): Boolean =
    if (isHnSEnabled)
      isComplete(Seq(
        isTrusteePartnershipDetailsComplete(index),
        isTrusteePartnershipAddressComplete(index),
        isTrusteePartnershipContactDetailsComplete(index))).getOrElse(false)
    else
      isTrusteePartnershipCompleteNonHns(index)
}
