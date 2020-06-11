/*
 * Copyright 2020 HM Revenue & Customs
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

package identifiers

import identifiers.register.trustees.{MoreThanTenTrusteesId, TrusteeKindId, TrusteesId}
import models.register.trustees.TrusteeKind
import play.api.libs.json.{JsResult, JsSuccess}
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import utils.{CountryOptions, Enumerable, UserAnswers}
import viewmodels.Message

case object HaveAnyTrusteesId extends TypedIdentifier[Boolean] with Enumerable.Implicits {
  self =>
  override def toString: String = "haveAnyTrustees"

  implicit def cya(implicit countryOptions: CountryOptions,
                   userAnswers: UserAnswers): CheckYourAnswers[self.type] =
    BooleanCYA[self.type](
      label = Some(Message("haveAnyTrustees.checkYourAnswersLabel", userAnswers.get(SchemeNameId).getOrElse(""))),
      hiddenLabel = Some(Message("messages__visuallyhidden__haveAnyTrustees", userAnswers.get(SchemeNameId)
        .getOrElse("")))
    )()

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        removeAllTrustees(userAnswers).flatMap(_.remove(MoreThanTenTrusteesId))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }

  private def removeAllTrustees(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.getAllRecursive[TrusteeKind](TrusteeKindId.collectionPath) match {
      case Some(allTrustees) if allTrustees.nonEmpty =>
        userAnswers.remove(TrusteesId(0)).flatMap(removeAllTrustees)
      case _ => JsSuccess(userAnswers)
    }
  }
}
