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

import identifiers.TypedIdentifier
import identifiers.register.trustees.{MoreThanTenTrusteesId, TrusteesId}
import models.person.PersonDetails
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class TrusteeDetailsId(index: Int) extends TypedIdentifier[PersonDetails] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeDetailsId.toString

  override def cleanup(value: Option[PersonDetails], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.allTrusteesAfterDelete.lengthCompare(10) match {
      case x if x<=0 => userAnswers.remove(MoreThanTenTrusteesId)
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object TrusteeDetailsId {
  override lazy val toString: String = "trusteeDetails"
}
