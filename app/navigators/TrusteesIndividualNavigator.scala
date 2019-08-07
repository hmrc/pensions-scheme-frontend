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

package navigators

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.routes.AddTrusteeController
import identifiers.Identifier
import identifiers.register.trustees.individual.TrusteeNameId
import models._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {
  import TrusteesIndividualNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeNameId(_) => addTrusteePage(mode, srn)
  }

  private def checkModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case _ => sessionExpiredPage
  }

  private def checkUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case _ => sessionExpiredPage
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = applyRoutes(normalAndUpdateModeRoutes, from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = applyRoutes(checkModeRoutes, from, CheckMode, None)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    applyRoutes(normalAndUpdateModeRoutes, from, UpdateMode, srn)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    applyRoutes(checkUpdateModeRoutes, from, CheckUpdateMode, srn)
}

object TrusteesIndividualNavigator {
  private def addTrusteePage(mode: Mode, srn: Option[String]): Call = AddTrusteeController.onPageLoad(mode, srn)
  private def sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()
}