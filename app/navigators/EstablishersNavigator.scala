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

package navigators

import identifiers.Identifier
import identifiers.register.establishers.EstablisherKindId
import models.NormalMode
import models.register.establishers.EstablisherKind
import play.api.mvc.Call
import utils.{Enumerable, Navigator, UserAnswers}

class EstablishersNavigator extends Navigator with Enumerable.Implicits {

  override protected def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case EstablisherKindId(index) => establisherKindRoutes(index)
  }

  private def establisherKindRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(EstablisherKindId(index)) match {
      case Some(EstablisherKind.Company) =>
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index)
      case Some(EstablisherKind.Indivdual) =>
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, index)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}
