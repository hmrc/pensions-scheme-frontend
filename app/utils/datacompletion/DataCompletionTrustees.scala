/*
 * Copyright 2023 HM Revenue & Customs
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

import identifiers.register.trustees.company._
import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership._
import utils.UserAnswers


trait DataCompletionTrustees {

  self: UserAnswers =>

  def isTrusteeCompanyComplete(index: Int): Boolean =
    isComplete(Seq(
      isTrusteeCompanyDetailsComplete(index),
      isTrusteeCompanyAddressComplete(index),
      isTrusteeCompanyContactDetailsComplete(index))).getOrElse(false)

  //TRUSTEE COMPANY
  def isTrusteeCompanyDetailsComplete(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(HasCompanyCRNId(index), CompanyEnterCRNId(index), Some(CompanyNoCRNReasonId(index))),
        isAnswerComplete(HasCompanyUTRId(index), CompanyEnterUTRId(index), Some(CompanyNoUTRReasonId(index))),
        isAnswerComplete(HasCompanyVATId(index), CompanyEnterVATId(index), None),
        isAnswerComplete(HasCompanyPAYEId(index), CompanyEnterPAYEId(index), None)
      )
    )

  def isTrusteeCompanyAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(
      CompanyAddressId(index),
      CompanyPreviousAddressId(index),
      CompanyAddressYearsId(index),
      Some(HasBeenTradingCompanyId(index)))

  def isTrusteeCompanyContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(CompanyEmailId(index), CompanyPhoneId(index))

  //TRUSTEE INDIVIDUAL

  def isTrusteeIndividualComplete(index: Int): Boolean =
    isComplete(
      Seq(
        isTrusteeIndividualDetailsComplete(index),
        isTrusteeIndividualAddressComplete(index),
        isTrusteeIndividualContactDetailsComplete(index)
      )
    ).getOrElse(false)

  def isTrusteeIndividualDetailsComplete(trusteeIndex: Int): Option[Boolean] = {
    isComplete(Seq(
      isAnswerComplete(TrusteeDOBId(trusteeIndex)),
      isAnswerComplete(TrusteeHasNINOId(trusteeIndex), TrusteeEnterNINOId(trusteeIndex), Some(TrusteeNoNINOReasonId
      (trusteeIndex))),
      isAnswerComplete(TrusteeHasUTRId(trusteeIndex), TrusteeUTRId(trusteeIndex), Some(TrusteeNoUTRReasonId
      (trusteeIndex)))
    ))
  }

  def isTrusteeIndividualAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(TrusteeAddressId(index),
      TrusteePreviousAddressId(index),
      TrusteeAddressYearsId(index),
      None)

  def isTrusteeIndividualContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(TrusteeEmailId(index), TrusteePhoneId(index))

  def isTrusteePartnershipComplete(index: Int): Boolean =
    isComplete(Seq(
      isTrusteePartnershipDetailsComplete(index),
      isTrusteePartnershipAddressComplete(index),
      isTrusteePartnershipContactDetailsComplete(index))).getOrElse(false)

  //TRUSTEE PARTNERSHIP
  def isTrusteePartnershipDetailsComplete(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(PartnershipHasUTRId(index), PartnershipEnterUTRId(index), Some(PartnershipNoUTRReasonId
        (index))),
        isAnswerComplete(PartnershipHasVATId(index), PartnershipEnterVATId(index), None),
        isAnswerComplete(PartnershipHasPAYEId(index), PartnershipEnterPAYEId(index), None)
      )
    )

  def isTrusteePartnershipAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(PartnershipAddressId(index), PartnershipPreviousAddressId(index), PartnershipAddressYearsId
    (index),
      Some(PartnershipHasBeenTradingId(index)))

  def isTrusteePartnershipContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(PartnershipEmailId(index), PartnershipPhoneId(index))
}
