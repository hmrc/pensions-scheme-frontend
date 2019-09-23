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

import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director._
import identifiers.register.trustees.company.CompanyUTRId
import identifiers.register.trustees.individual._
import models._
import models.address.Address
import models.person.PersonName
import models.register.DeclarationDormant
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
        .set(TrusteeNewNinoId(index))(ReferenceValue(stringValue))
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

  protected def setCompanyCompletionStatusCompanyDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setCompanyCompletionStatusJsResultCompanyDetails(isComplete, index, ua).asOpt.value

  protected def setCompanyCompletionStatusAddressDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setCompanyCompletionStatusJsResultAddressDetails(isComplete, index, ua).asOpt.value

  protected def setCompanyCompletionStatusContactDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setCompanyCompletionStatusJsResultContactDetails(isComplete, index, ua).asOpt.value

  protected def setCompanyCompletionStatusDirectorDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): UserAnswers =
    setCompanyCompletionStatusJsResultDirectorDetails(isComplete, index, ua).asOpt.value

  protected def setCompanyCompletionStatusJsResultCompanyDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(HasCompanyNumberId(index))(false)
        .asOpt
        .value
        .set(NoCompanyNumberId(index))(stringValue)
        .asOpt
        .value
        .set(HasCompanyUTRId(index))(false)
        .asOpt
        .value
        .set(NoCompanyUTRId(index))(stringValue)
        .asOpt
        .value
        .set(HasCompanyVATId(index))(true)
        .asOpt
        .value
        .set(CompanyEnterVATId(index))(ReferenceValue(stringValue))
        .asOpt
        .value
        .set(HasCompanyPAYEId(index))(true)
        .asOpt
        .value
        .set(CompanyPayeVariationsId(index))(ReferenceValue(stringValue))
        .asOpt
        .value
        .set(IsCompanyDormantId(index))(DeclarationDormant.No)
    }
    else {
      ua.set(HasCompanyNumberId(index))(false)
        .asOpt
        .value
        .set(NoCompanyNumberId(index))(stringValue)
        .asOpt
        .value
        .set(HasCompanyUTRId(index))(false)
        .asOpt
        .value
//        .set(NoCompanyUTRId(index))(stringValue)
//        .asOpt
//        .value
        .set(HasCompanyVATId(index))(true)
        .asOpt
        .value
        .set(CompanyEnterVATId(index))(ReferenceValue(stringValue))
        .asOpt
        .value
        .set(HasCompanyPAYEId(index))(true)
        .asOpt
        .value
        .set(CompanyPayeVariationsId(index))(ReferenceValue(stringValue))
        .asOpt
        .value
        .set(IsCompanyDormantId(index))(DeclarationDormant.No)
    }

  protected def setCompanyCompletionStatusJsResultAddressDetails(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(CompanyAddressId(index))(address)
        .asOpt
        .value
        .set(CompanyAddressYearsId(index))(AddressYears.OverAYear)
    }
    else {
      ua.set(CompanyAddressId(index))(address)
    }

  protected def setCompanyCompletionStatusJsResultContactDetails(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(CompanyEmailId(index))(stringValue)
        .asOpt
        .value
        .set(CompanyPhoneId(index))(stringValue)
    }
    else {
      ua.set(CompanyPhoneId(index))(stringValue)
    }

  protected def setCompanyCompletionStatusJsResultDirectorDetails(isComplete: Boolean, index: Int = 0, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    if (isComplete) {
      ua.set(DirectorNameId(0, 0))(PersonName(stringValue, stringValue))
        .asOpt
        .value
        .set(DirectorDOBId(0, 0))(dateValue)
        .asOpt
        .value
        .set(DirectorHasNINOId(0, 0))(true)
        .asOpt
        .value
        .set(DirectorNewNinoId(0, 0))(refValue)
        .asOpt
        .value
        .set(DirectorHasUTRId(0, 0))(true)
        .asOpt
        .value
        .set(DirectorUTRId(0, 0))(refValue)
        .asOpt
        .value
        .set(DirectorAddressId(0, 0))(address)
        .asOpt
        .value
        .set(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear)
        .asOpt
        .value
        .set(DirectorEmailId(0, 0))(stringValue)
        .asOpt
        .value
        .set(DirectorPhoneNumberId(0, 0))(stringValue)
    }
    else {
      ua.set(DirectorNameId(0, 0))(PersonName(stringValue, stringValue))
        .asOpt
        .value
        .set(DirectorDOBId(0, 0))(dateValue)
        .asOpt
        .value
        .set(DirectorHasNINOId(0, 0))(true)
        .asOpt
        .value
        .set(DirectorNewNinoId(0, 0))(refValue)
        .asOpt
        .value
        .set(DirectorHasUTRId(0, 0))(true)
        .asOpt
        .value
        .set(DirectorAddressId(0, 0))(address)
        .asOpt
        .value
        .set(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear)
        .asOpt
        .value
        .set(DirectorEmailId(0, 0))(stringValue)
        .asOpt
        .value
        .set(DirectorPhoneNumberId(0, 0))(stringValue)
    }

  protected def setCompanyCompletionStatusJsResult(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): JsResult[UserAnswers] =
    setCompanyCompletionStatusJsResultDirectorDetails(
      isComplete,
      index,
      setCompanyCompletionStatusJsResultContactDetails(
        isComplete,
        index,
        setCompanyCompletionStatusJsResultAddressDetails(isComplete, index,
          setCompanyCompletionStatusJsResultCompanyDetails(isComplete, index, ua).asOpt.value).asOpt.value
      ).asOpt.value
    )

  protected def setCompanyCompletionStatus(isComplete: Boolean, index: Int, ua: UserAnswers = UserAnswers()): UserAnswers =
    setCompanyCompletionStatusJsResult(isComplete, index, ua).asOpt.value
}
