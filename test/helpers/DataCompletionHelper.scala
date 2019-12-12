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

package helpers

import identifiers.register.trustees.individual._
import models._
import models.address.Address
import models.register.SchemeType
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.libs.json.JsResult
import utils.UserAnswers

trait DataCompletionHelper extends OptionValues {
  private val address     = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val stringValue = "value"
  private val refValue    = ReferenceValue(stringValue)
  private val firstName   = "firstName"
  private val lastName    = "lastName"
  private val dateValue   = new LocalDate(2000, 6, 9)

  protected def setTrusteeCompletionStatusIndividualDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultIndividualDetails(isComplete, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusAddressDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultAddressDetails(isComplete, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusContactDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultContactDetails(isComplete, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusJsResultIndividualDetails(isComplete: Boolean,
                                                                    index: Int = 0,
                                                                    ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(TrusteeDOBId(index))(LocalDate.now())
        .asOpt
        .value
        .set(TrusteeHasNINOId(index))(true)
        .asOpt
        .value
        .set(TrusteeEnterNINOId(index))(ReferenceValue(stringValue))
        .asOpt
        .value
        .set(TrusteeHasUTRId(index))(true)
        .asOpt
        .value
        .set(TrusteeUTRId(index))(ReferenceValue(stringValue))
    }
    else {
      ua.set(TrusteeDOBId(index))(LocalDate.now())
        .asOpt
        .value
        .set(TrusteeHasNINOId(index))(true)
        .asOpt
        .value
        .set(TrusteeHasUTRId(index))(true)
        .asOpt
        .value
        .set(TrusteeUTRId(index))(ReferenceValue(stringValue))
    }

  protected def setTrusteeCompletionStatusJsResultAddressDetails(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(TrusteeAddressId(index))(address)
        .asOpt
        .value
        .set(TrusteeAddressYearsId(index))(AddressYears.OverAYear)
    }
    else {
      ua.set(TrusteeAddressId(index))(address)
    }

  protected def setTrusteeCompletionStatusJsResultContactDetails(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(TrusteeEmailId(index))(stringValue)
        .asOpt
        .value
        .set(TrusteePhoneId(index))(stringValue)
    }
    else {
      ua.set(TrusteePhoneId(index))(stringValue)
    }

  protected def setTrusteeCompletionStatusJsResult(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    setTrusteeCompletionStatusJsResultContactDetails(
      isComplete,
      index,
      setTrusteeCompletionStatusJsResultAddressDetails(isComplete,
                                                       index,
                                                       setTrusteeCompletionStatusJsResultIndividualDetails(isComplete, index, ua).asOpt.value).asOpt.value
    )

  protected def setTrusteeCompletionStatus(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResult(isComplete, index, ua).asOpt.value

  protected def setCompleteBeforeYouStart(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if(isComplete) {
      ua.schemeName(schemeName = "Test Scheme").
        schemeType(SchemeType.SingleTrust).establishedCountry(country = "GB").
        declarationDuties(haveWorkingKnowledge = true)
    } else {
      ua.schemeName(schemeName = "Test Scheme")
    }
  }

  protected def setCompleteMembers(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if(isComplete) ua.currentMembers(Members.One).futureMembers(Members.One) else ua.currentMembers(Members.One)
  }

  protected def setCompleteBank(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if(isComplete) ua.ukBankAccount(ukBankAccount = false) else ua.ukBankAccount(ukBankAccount = true)
  }

  protected def setCompleteBenefits(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if(isComplete) {
      ua.occupationalPensionScheme(isOccupational = true).
        investmentRegulated(isInvestmentRegulated = true).typeOfBenefits(TypeOfBenefits.MoneyPurchase).
        benefitsSecuredByInsurance(isInsured = false)
    } else {
      ua.occupationalPensionScheme(isOccupational = true)
    }
  }

  protected def setCompleteWorkingKnowledge(isComplete: Boolean, ua: UserAnswers): UserAnswers = {
    if(isComplete) {
      ua.adviserName(name = "test adviser").adviserEmailAddress(email = "s@s.com").
        adviserPhone("123").advisersAddress(Address("a", "b", None, None, None, "GB"))
    } else {
      ua.adviserName(name = "test adviser")
    }
  }
}
