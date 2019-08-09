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
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.libs.json.JsResult
import utils.UserAnswers

trait DataCompletionHelper extends OptionValues {
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val stringValue = "value"
  private val firstName = "firstName"
  private val lastName = "lastName"

  protected def setTrusteeCompletionStatusIndividualDetails(isComplete: Boolean, toggled: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultIndividualDetails(isComplete, toggled, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusAddressDetails(isComplete: Boolean, toggled: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultAddressDetails(isComplete, toggled, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusContactDetails(isComplete: Boolean, toggled: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResultContactDetails(isComplete, toggled, index, ua).asOpt.value

  protected def setTrusteeCompletionStatusJsResultIndividualDetails(isComplete: Boolean, toggled: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      if (toggled) {
        ua.
          set(TrusteeDOBId(index))(LocalDate.now()).asOpt.value
          .set(TrusteeHasNINOId(index))(true).asOpt.value
          .set(TrusteeNewNinoId(index))(ReferenceValue(stringValue)).asOpt.value
          .set(TrusteeHasUTRId(index))(true).asOpt.value
          .set(TrusteeUTRId(index))(stringValue)
      } else {
        ua.
          set(TrusteeDetailsId(index))(PersonDetails(firstName, None, lastName, LocalDate.now())).asOpt.value
          .set(TrusteeNinoId(index))(Nino.Yes(stringValue)).asOpt.value
          .set(UniqueTaxReferenceId(index))(UniqueTaxReference.Yes(stringValue))
      }
    } else {
      if (toggled) {
        ua.
          set(TrusteeDOBId(index))(LocalDate.now()).asOpt.value
          .set(TrusteeHasNINOId(index))(true).asOpt.value
          .set(TrusteeHasUTRId(index))(true).asOpt.value
          .set(TrusteeUTRId(index))(stringValue)
      } else {
        ua.
          set(TrusteeDetailsId(index))(PersonDetails(firstName, None, lastName, LocalDate.now())).asOpt.value
          .set(UniqueTaxReferenceId(index))(UniqueTaxReference.Yes(stringValue))
      }
    }

  protected def setTrusteeCompletionStatusJsResultAddressDetails(isComplete: Boolean, toggled: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua
        .set(TrusteeAddressId(index))(address).asOpt.value
        .set(TrusteeAddressYearsId(index))(AddressYears.OverAYear)
    } else {
      ua
        .set(TrusteeAddressId(index))(address)
    }

  protected def setTrusteeCompletionStatusJsResultContactDetails(isComplete: Boolean, toggled: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      if (toggled) {
        ua
          .set(TrusteeEmailId(index))(stringValue).asOpt.value
          .set(TrusteePhoneId(index))(stringValue)
      } else {
        ua
          .set(TrusteeContactDetailsId(index))(ContactDetails(stringValue, stringValue))
      }
    } else {
      if (toggled) {
        ua
          .set(TrusteePhoneId(index))(stringValue)
      } else {
        ua
          .set(TrusteeContactDetailsId(index))(ContactDetails(stringValue, stringValue))
      }
    }

  protected def setTrusteeCompletionStatusJsResult(isComplete: Boolean, toggled: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    setTrusteeCompletionStatusJsResultContactDetails(isComplete, toggled, index,
      setTrusteeCompletionStatusJsResultAddressDetails(isComplete, toggled, index,
        setTrusteeCompletionStatusJsResultIndividualDetails(isComplete, toggled, index, ua).asOpt.value).asOpt.value)

  protected def setTrusteeCompletionStatus(isComplete: Boolean, toggled: Boolean, index: Int, ua: UserAnswers = UserAnswers()): UserAnswers =
    setTrusteeCompletionStatusJsResult(isComplete, toggled, index, ua).asOpt.value
}
