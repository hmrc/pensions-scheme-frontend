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

package navigators.trustees.individuals

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.individual.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class TrusteesIndividualContactDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
            extends AbstractNavigator {

  import TrusteesIndividualContactDetailsNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode,
                                       ua: UserAnswers,
                                       srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeEmailId(index) if mode == NormalMode => phonePage(mode, index, srn)
    case TrusteeEmailId(index) => cyaIndividualContactDetailsPage(mode, index, srn)
    case TrusteePhoneId(index) => cyaIndividualContactDetailsPage(mode, index, srn)
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: UpdateMode.type,
                               ua: UserAnswers,
                               srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeEmailId(index) => phonePage(mode, index, srn)
    case TrusteePhoneId(index) => cyaIndividualContactDetailsPage(mode, index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type,
                                   ua: UserAnswers,
                                   srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeEmailId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false) =>
      cyaIndividualContactDetailsPage(mode, index, srn)
    case TrusteeEmailId(_) =>
      anyMoreChangesPage(srn)
    case TrusteePhoneId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false) =>
      cyaIndividualContactDetailsPage(mode, index, srn)
    case TrusteePhoneId(_) =>
      anyMoreChangesPage(srn)
  }
}

object TrusteesIndividualContactDetailsNavigator {
  private def phonePage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteePhoneController.onPageLoad(mode, index, srn)

  private def cyaIndividualContactDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call =
    CheckYourAnswersIndividualContactDetailsController.onPageLoad(journeyMode(mode), index, srn)
}

