/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class IndividualConfirmPreviousAddressId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath =
    EstablishersId(index).path \ IndividualConfirmPreviousAddressId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers
          .remove(PreviousAddressId(index))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

case object IndividualConfirmPreviousAddressId {
  override def toString: String = "individualConfirmPreviousAddress"
}
