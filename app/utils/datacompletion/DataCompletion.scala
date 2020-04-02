/*
 * Copyright 2020 HM Revenue & Customs
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

import identifiers._
import identifiers.register.establishers.company._
import identifiers.register.trustees.{company => tc}
import identifiers.register.trustees.{partnership => tp}
import identifiers.register.establishers.company.director._
import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership.PartnershipHasBeenTradingId
import models.address.Address
import models._
import play.api.libs.json.Reads
import utils.UserAnswers

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

  def isAnswerComplete[A](yesNoQuestionId: TypedIdentifier[Boolean],
                              yesValueId: TypedIdentifier[A],
                              noReasonIdOpt: Option[TypedIdentifier[String]])(implicit reads: Reads[A]): Option[Boolean] =
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

  def isBeforeYouStartCompleted(mode: Mode): Boolean = {

    val isSingleOrMaster = schemeType.fold(false)(scheme => Seq("single", "master").exists(_.equals(scheme)))
    val haveAnyTrusteeComplete = if (mode == UpdateMode) {
      true
    } else {
      if (isSingleOrMaster && get(HaveAnyTrusteesId).isEmpty) true else get(HaveAnyTrusteesId).nonEmpty
    }
    val declarationDutiesComplete = if (mode == UpdateMode) true else get(DeclarationDutiesId).nonEmpty
    !List(get(SchemeNameId), get(SchemeTypeId), get(EstablishedCountryId)).contains(None) &&
      haveAnyTrusteeComplete && declarationDutiesComplete
  }

  def isMembersCompleted: Option[Boolean] = isComplete(Seq(
    isAnswerComplete(CurrentMembersId),
    isAnswerComplete(FutureMembersId)))

  def isBankDetailsCompleted: Option[Boolean] = isAnswerComplete(UKBankAccountId, BankAccountDetailsId, None)

  def isBenefitsAndInsuranceCompleted: Option[Boolean] = {

    val isBenefitsSecuredByContractCompleted = get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => isComplete(Seq(
        isAnswerComplete(InsuranceCompanyNameId), isAnswerComplete(InsurancePolicyNumberId), isAnswerComplete(InsurerConfirmAddressId)))
      case Some(false) => Some(true)
      case _ => None
    }

    isComplete(Seq(
      isAnswerComplete(InvestmentRegulatedSchemeId),
      isAnswerComplete(OccupationalPensionSchemeId),
      isAnswerComplete(TypeOfBenefitsId),
      isBenefitsSecuredByContractCompleted))
  }

  def isAdviserCompleted: Option[Boolean] = isComplete(Seq(
    isAnswerComplete(AdviserNameId),
    isAnswerComplete(AdviserEmailId),
    isAnswerComplete(AdviserPhoneId),
    isAnswerComplete(AdviserAddressId)
  ))

  def isWorkingKnowledgeCompleted: Option[Boolean] = get(DeclarationDutiesId) match {
    case Some(false) => isAdviserCompleted
    case _ => get(DeclarationDutiesId)
  }
}
