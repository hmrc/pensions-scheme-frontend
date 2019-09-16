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

import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership.PartnershipHasBeenTradingId
import identifiers.register.trustees.{company => tc, partnership => tp}
import utils.UserAnswers

trait DataCompletionTrustees extends DataCompletion {

  self: UserAnswers =>

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
