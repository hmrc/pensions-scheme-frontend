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

package navigators

import com.google.inject.Inject
import identifiers.Identifier
import models.requests.IdentifiedRequest
import models.{Mode, OptionalSchemeReferenceNumber}
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

class CompoundNavigator @Inject()(navigators: java.util.Set[Navigator]) extends Navigator {

  override def nextPageOptional(id: Identifier,
                                mode: Mode,
                                userAnswers: UserAnswers,
                                srn: OptionalSchemeReferenceNumber)
                               (implicit ex: IdentifiedRequest,
                                ec: ExecutionContext, hc: HeaderCarrier): Option[Call] = {
    navigators.asScala.foldRight(Option.empty[Call]) {
      case (_, some: Some[Call]) => some
      case (navigator, None) => navigator.nextPageOptional(id, mode, userAnswers, srn)
    }
  }
}

