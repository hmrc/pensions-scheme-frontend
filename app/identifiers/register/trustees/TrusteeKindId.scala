/*
 * Copyright 2018 HM Revenue & Customs
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
import models.register.trustees.TrusteeKind
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class TrusteeKindId(index: Int) extends TypedIdentifier[TrusteeKind] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeKindId.toString

  override def cleanup(value: Option[TrusteeKind], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(TrusteeKind.Individual) =>
        userAnswers.remove(CompanyDetailsId(index)).flatMap(
          _.remove(CompanyRegistrationNumberId(index))).flatMap(
          _.remove(CompanyUniqueTaxReferenceId(index))).flatMap(
          _.remove(CompanyPostcodeLookupId(index))).flatMap(
          _.remove(CompanyAddressId(index))).flatMap(
          _.remove(CompanyAddressYearsId(index))).flatMap(
          _.remove(CompanyPreviousAddressPostcodeLookupId(index))).flatMap(
          _.remove(CompanyPreviousAddressId(index))).flatMap(
          _.remove(CompanyContactDetailsId(index))
        )
      case Some(TrusteeKind.Company) =>
        userAnswers.remove(TrusteeDetailsId(index)).flatMap(
          _.remove(TrusteeNinoId(index))).flatMap(
          _.remove(UniqueTaxReferenceId(index))).flatMap(
          _.remove(IndividualPostCodeLookupId(index))).flatMap(
          _.remove(TrusteeAddressId(index))).flatMap(
          _.remove(TrusteeAddressYearsId(index))).flatMap(
          _.remove(IndividualPreviousAddressPostCodeLookupId(index))).flatMap(
          _.remove(TrusteePreviousAddressId(index))).flatMap(
          _.remove(TrusteeContactDetailsId(index))
        )
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeKindId {
  override def toString: String = "trusteeKind"
}
