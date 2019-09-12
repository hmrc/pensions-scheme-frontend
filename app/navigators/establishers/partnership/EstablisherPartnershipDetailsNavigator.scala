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

package navigators.establishers.partnership

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import identifiers.Identifier
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class EstablisherPartnershipDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import EstablisherPartnershipDetailsNavigator._

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(_) => {
      println("\n\n\nin new nav")
      addEstablisherPage(mode, srn)
    }
  }

  private def updateModeRoutes(mode: VarianceMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(_) => addEstablisherPage(mode, srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    println("\n\n route map")
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}

object EstablisherPartnershipDetailsNavigator {
  private def addEstablisherPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)
}

