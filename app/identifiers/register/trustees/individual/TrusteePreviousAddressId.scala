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

package identifiers.register.trustees.individual

import identifiers._
import identifiers.register.trustees.TrusteesId
import models.address.{Address, TolerantAddress}
import play.api.libs.json.JsPath
import utils.CountryOptions
import utils.checkyouranswers.CheckYourAnswers

case class TrusteePreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ TrusteePreviousAddressId.toString
}

object TrusteePreviousAddressId {
  override def toString: String = "trusteePreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[TrusteePreviousAddressId] =
    CheckYourAnswers.address("messages__common__cya__previous_address")
}
