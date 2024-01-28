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

package identifiers.register.trustees

import identifiers.TypedIdentifier
import models.register.trustees.TrusteeKind
import play.api.libs.json.{JsResult, JsSuccess}
import utils.{Enumerable, UserAnswers}

case object HaveAnyTrusteesId extends TypedIdentifier[Boolean] with Enumerable.Implicits {
  override def toString: String = "haveAnyTrustees"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        removeAllTrustees(userAnswers).flatMap(
          _.remove(MoreThanTenTrusteesId)
        )
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }

  private def removeAllTrustees(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.getAllRecursive[TrusteeKind](TrusteeKindId.collectionPath) match {
      case Some(allTrustees) if allTrustees.nonEmpty =>
        userAnswers.remove(TrusteesId(0)).flatMap(removeAllTrustees)
      case _ =>
        JsSuccess(userAnswers)
    }
  }
}
