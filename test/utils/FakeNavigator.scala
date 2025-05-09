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

package utils

import identifiers.Identifier
import models.requests.IdentifiedRequest
import models.{Mode, NormalMode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class FakeNavigator(desiredRoute: Call, val mode: Mode = NormalMode) extends Navigator {

  private var userAnswers: Option[UserAnswers] = None

  def lastUserAnswers: Option[UserAnswers] = userAnswers

  override def nextPage(id: Identifier, mode: Mode, answers: UserAnswers, srn: OptionalSchemeReferenceNumber)
                       (implicit ex: IdentifiedRequest, ec: ExecutionContext, hc: HeaderCarrier): Call = {
    userAnswers = Some(answers)
    desiredRoute
  }

  override def nextPageOptional(id: Identifier, mode: Mode, userAnswers: UserAnswers, srn: OptionalSchemeReferenceNumber)
                               (implicit ex: IdentifiedRequest, ec: ExecutionContext, hc: HeaderCarrier): Option[Call] =
    Some(desiredRoute)
}

object FakeNavigator extends FakeNavigator(Call("GET", "www.example.com"), NormalMode)
