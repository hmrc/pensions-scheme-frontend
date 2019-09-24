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

package identifiers.register.trustees

import identifiers._
import identifiers.register.trustees.company._
import identifiers.register.trustees.individual._
import identifiers.register.trustees.partnership._
import models.Index
import models.register.trustees.TrusteeKind
import play.api.libs.json.{JsPath, JsResult, __}
import utils.UserAnswers

case class TrusteeKindId(index: Int) extends TypedIdentifier[TrusteeKind] {

  import TrusteeKindId._

  override def path: JsPath = TrusteesId(index).path \ TrusteeKindId.toString

  override def cleanup(value: Option[TrusteeKind], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(TrusteeKind.Individual) =>
        userAnswers.removeAllOf(companyIdList(index) ++ partnershipIdList(index))
      case Some(TrusteeKind.Company) =>
        userAnswers.removeAllOf(individualIdList(index) ++ partnershipIdList(index))
      case Some(TrusteeKind.Partnership) =>
        userAnswers.removeAllOf(companyIdList(index) ++ individualIdList(index))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeKindId {
  private def partnershipIdList(index: Index): List[TypedIdentifier[_]] = List(
    PartnershipDetailsId(index),
    PartnershipPostcodeLookupId(index), PartnershipAddressListId(index), PartnershipAddressId(index), PartnershipAddressYearsId(index),
    PartnershipPreviousAddressPostcodeLookupId(index), PartnershipPreviousAddressId(index), PartnershipPreviousAddressListId(index),
    PartnershipContactDetailsId(index)
  )

  // TODO 3341: Deal with email and phone id
  private def companyIdList(index: Index): List[TypedIdentifier[_]] = List(
    CompanyDetailsId(index),
    HasCompanyCRNId(index),
    CompanyEnterCRNId(index),
    HasCompanyUTRId(index),
    CompanyEnterUTRId(index),
    CompanyPostcodeLookupId(index),
    CompanyAddressId(index),
    CompanyAddressYearsId(index),
    CompanyPreviousAddressPostcodeLookupId(index),
    CompanyPreviousAddressId(index)
//    CompanyEmailId(index)
//    company.CompanyPhoneId(index)
  )

  private def individualIdList(index: Index): List[TypedIdentifier[_]] = List(
    TrusteeNameId(index),
    TrusteeEnterNINOId(index),
    IndividualPostCodeLookupId(index),
    TrusteeAddressId(index),
    TrusteeAddressYearsId(index),
    IndividualPreviousAddressPostCodeLookupId(index),
    TrusteePreviousAddressId(index)
  )

  def collectionPath: JsPath = __ \ TrusteesId.toString \\ TrusteeKindId.toString

  override def toString: String = "trusteeKind"
}
