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

package identifiers.register.establishers.company.director

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.libs.json.JsPath
import utils.CountryOptions
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}

case class DirectorAddressId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorAddressId.toString
}

object DirectorAddressId {
  override def toString: String = "directorAddressId"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[DirectorAddressId] = {
    AddressCYA[DirectorAddressId](changeAddress = "messages__visuallyhidden__director__address")()
  }
}
