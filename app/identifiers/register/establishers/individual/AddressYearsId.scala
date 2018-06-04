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

package identifiers.register.establishers.individual

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.AddressYears
import play.api.libs.json._
import utils.UserAnswers

case class AddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {

  override def path: JsPath = EstablishersId(index).path \ AddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers
          .remove(PreviousPostCodeLookupId(this.index))
          .flatMap(_.remove(PreviousAddressId(this.index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object AddressYearsId {

  override lazy val toString: String =
    "addressYears"

}
