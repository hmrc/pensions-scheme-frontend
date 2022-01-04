/*
 * Copyright 2022 HM Revenue & Customs
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

package models.details

import play.api.libs.json._

case class SchemeMemberNumbers(current: String, future: String)

object SchemeMemberNumbers {
  implicit val formats: OFormat[SchemeMemberNumbers] = Json.format[SchemeMemberNumbers]
}

case class InsuranceCompany(name: Option[String], policyNumber: Option[String], address: Option[CorrespondenceAddress])

object InsuranceCompany {
  implicit val formats: OFormat[InsuranceCompany] = Json.format[InsuranceCompany]
}

case class SchemeDetails(srn: Option[String],
                         pstr: Option[String],
                         status: String,
                         name: String,
                         isMasterTrust: Boolean,
                         typeOfScheme: Option[String],
                         otherTypeOfScheme: Option[String],
                         hasMoreThanTenTrustees: Boolean,
                         members: SchemeMemberNumbers,
                         isInvestmentRegulated: Boolean,
                         isOccupational: Boolean,
                         benefits: String,
                         country: String,
                         areBenefitsSecured: Boolean,
                         insuranceCompany: Option[InsuranceCompany]) {
}

object SchemeDetails {
  implicit val formats: OFormat[SchemeDetails] = Json.format[SchemeDetails]
}
