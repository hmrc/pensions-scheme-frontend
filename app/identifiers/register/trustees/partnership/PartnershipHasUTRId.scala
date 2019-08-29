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

package identifiers.register.trustees.partnership

import identifiers._
import identifiers.register.trustees.TrusteesId
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class PartnershipHasUTRId(index: Int) extends TypedIdentifier[Boolean] {

  override def path: JsPath = TrusteesId(index).path \ PartnershipHasUTRId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {

      case Some(true) => userAnswers.remove(PartnershipNoUTRReasonId(index))

      case Some(false) => userAnswers.remove(PartnershipUTRId(index))

      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnershipHasUTRId {
  override def toString: String = "hasUtr"
}


