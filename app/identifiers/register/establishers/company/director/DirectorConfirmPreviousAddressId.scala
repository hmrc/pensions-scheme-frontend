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

package identifiers.register.establishers.company.director

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class DirectorConfirmPreviousAddressId(establisherIndex: Int, directorIndex: Int) extends
  TypedIdentifier[Boolean] {

  override def path: JsPath =
    EstablishersId(establisherIndex)
      .path \ "director" \ directorIndex \ DirectorConfirmPreviousAddressId.toString

  override def toString: String = "directorConfirmPreviousAddress"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers
          .remove(DirectorPreviousAddressId(establisherIndex, directorIndex))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}
