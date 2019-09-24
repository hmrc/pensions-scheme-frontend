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

import identifiers.register.trustees.company._
import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership._
import utils.UserAnswers


trait DataCompletionTrustees {

  self: UserAnswers =>

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
    isAddressComplete(CompanyAddressId(index), CompanyPreviousAddressId(index), CompanyAddressYearsId(index), Some(HasBeenTradingCompanyId(index)))

  def isTrusteeCompanyContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(CompanyEmailId(index), CompanyPhoneId(index))

  def isTrusteeCompanyCompleteNonHns(index: Int): Boolean =
    isListComplete(Seq(
      get(CompanyDetailsId(index)).isDefined,
      get(CompanyEnterCRNId(index)).isDefined,
      get(HasCompanyUTRId(index)).isDefined | get(CompanyEnterUTRId(index)).isDefined,
      get(HasCompanyVATId(index)).isDefined | get(CompanyEnterVATId(index)).isDefined,
      get(HasCompanyPAYEId(index)).isDefined | get(CompanyEnterPAYEId(index)).isDefined,
      isAddressComplete(CompanyAddressId(index), CompanyPreviousAddressId(index), CompanyAddressYearsId(index), None).getOrElse(false),
      get(CompanyEmailId(index)).isDefined,
      get(CompanyPhoneId(index)).isDefined
    ))

  def isTrusteeCompanyComplete(index: Int): Boolean =
      isComplete(Seq(
        isTrusteeCompanyDetailsComplete(index),
        isTrusteeCompanyAddressComplete(index),
        isTrusteeCompanyContactDetailsComplete(index))).getOrElse(false)

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

  def isTrusteeIndividualComplete(index: Int): Boolean =
      isComplete(
        Seq(
          isTrusteeIndividualDetailsComplete(index),
          isTrusteeIndividualAddressComplete(index),
          isTrusteeIndividualContactDetailsComplete(index)
        )
      ).getOrElse(false)

  //TRUSTEE PARTNERSHIP
  def isTrusteePartnershipDetailsComplete(index: Int): Option[Boolean] =
    isComplete(
      Seq(
        isAnswerComplete(PartnershipHasUTRId(index), PartnershipUTRId(index), Some(PartnershipNoUTRReasonId(index))),
        isAnswerComplete(PartnershipHasVATId(index), PartnershipEnterVATId(index), None),
        isAnswerComplete(PartnershipHasPAYEId(index), PartnershipPayeVariationsId(index), None)
      )
    )

  def isTrusteePartnershipAddressComplete(index: Int): Option[Boolean] =
    isAddressComplete(PartnershipAddressId(index), PartnershipPreviousAddressId(index), PartnershipAddressYearsId(index),
      Some(PartnershipHasBeenTradingId(index)))

  def isTrusteePartnershipContactDetailsComplete(index: Int): Option[Boolean] =
    isContactDetailsComplete(PartnershipEmailId(index), PartnershipPhoneId(index))

  def isTrusteePartnershipCompleteNonHns(index: Int): Boolean =
    isListComplete(Seq(
      get(PartnershipDetailsId(index)).isDefined,
      get(PartnershipUniqueTaxReferenceId(index)).isDefined | get(PartnershipUTRId(index)).isDefined,
      get(PartnershipVatId(index)).isDefined | get(PartnershipEnterVATId(index)).isDefined,
      get(PartnershipPayeId(index)).isDefined | get(PartnershipPayeVariationsId(index)).isDefined,
      isAddressComplete(PartnershipAddressId(index), PartnershipPreviousAddressId(index), PartnershipAddressYearsId(index), None).getOrElse(false),
      get(PartnershipContactDetailsId(index)).isDefined
    ))

  def isTrusteePartnershipComplete(index: Int): Boolean =
      isComplete(Seq(
        isTrusteePartnershipDetailsComplete(index),
        isTrusteePartnershipAddressComplete(index),
        isTrusteePartnershipContactDetailsComplete(index))).getOrElse(false)
}
