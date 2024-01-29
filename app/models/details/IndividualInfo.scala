/*
 * Copyright 2024 HM Revenue & Customs
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

case class IndividualInfo(personalDetails: PersonalInfo,
                          nino: Option[String],
                          utr: Option[String],
                          address: CorrespondenceAddress,
                          contact: IndividualContactDetails,
                          previousAddress: PreviousAddressInfo)

object IndividualInfo {
  implicit val formats: OFormat[IndividualInfo] = Json.format[IndividualInfo]
}

case class IndividualContactDetails(telephone: String, email: String)

object IndividualContactDetails {
  implicit val formats: OFormat[IndividualContactDetails] = Json.format[IndividualContactDetails]
}

case class PersonalInfo(name: IndividualName, dateOfBirth: String)

object PersonalInfo {
  implicit val formats: OFormat[PersonalInfo] = Json.format[PersonalInfo]
}

case class IndividualName(firstName: String, middleName: Option[String], lastName: String)

object IndividualName {
  implicit val formats: OFormat[IndividualName] = Json.format[IndividualName]
}

