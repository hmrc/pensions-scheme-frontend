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

package utils

import identifiers.TypedIdentifier
import models.{CheckUpdateMode, Mode, UpdateMode}
import models.requests.DataRequest
import play.api.mvc.AnyContent

trait AllowChangeHelper {
  def hideChangeLinks(request: DataRequest[AnyContent], userAnswers: UserAnswers, newId: TypedIdentifier[Boolean]):Boolean
  def hideSaveAndContinueButton(request: DataRequest[AnyContent], userAnswers: UserAnswers, newId: TypedIdentifier[Boolean], mode:Mode):Boolean
}

class AllowChangeHelperImpl extends AllowChangeHelper {
  private def hideItem(request: DataRequest[AnyContent], userAnswers: UserAnswers, newId: TypedIdentifier[Boolean]): Option[Boolean] =
    (request.viewOnly, userAnswers.get(newId)) match {
      case (true, _) => Some(true)
      case (_, Some(true)) => Some(false)
      case _ => None
    }

  def hideChangeLinks(request: DataRequest[AnyContent], userAnswers: UserAnswers, newId: TypedIdentifier[Boolean]):Boolean =
    hideItem(request, userAnswers, newId).getOrElse(false)

  def hideSaveAndContinueButton(request: DataRequest[AnyContent], userAnswers: UserAnswers, newId: TypedIdentifier[Boolean], mode:Mode):Boolean =
    hideItem(request, userAnswers, newId).getOrElse(mode == UpdateMode || mode == CheckUpdateMode)
}
