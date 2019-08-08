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
import controllers.register.trustees.individual.routes._
import identifiers.Identifier
import identifiers.register.trustees.individual._
import models._
import play.api.mvc.Call
import utils.UserAnswers
import models.Mode._

class TrusteesIndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import TrusteesIndividualNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeDOBId(index) => hasNinoPage(mode, index, srn)
    case id@TrusteeHasNINOId(index) => booleanNav(id, ua, mode, index, srn, ninoPage, noNinoReasonPage)
    case TrusteeNoNINOReasonId(index) => cyaAddressPage(mode, index, srn)

  }


  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = applyRoutes(normalAndUpdateModeRoutes, from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = None

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}

object TrusteesIndividualNavigator {
  private def hasNinoPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeHasNINOController.onPageLoad(mode, index, srn)

  private def ninoPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNinoController.onPageLoad(mode, index, srn)

  private def noNinoReasonPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNoNINOReasonController.onPageLoad(mode, index, srn)

  private def cyaAddressPage(mode: Mode, index: Int, srn: Option[String]): Call = CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
}
